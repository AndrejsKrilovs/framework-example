package krilovs.andrejs.framework.config.logging;

import krilovs.andrejs.framework.config.PropertiesConfig;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One context-rich event per request ("canonical log line").
 * Created by {@link krilovs.andrejs.framework.config.logging.WideEventFilter}, enriched by any code on the request thread
 * via {@link #current()}, emitted exactly once when the request finishes.
 */
public final class WideEvent {

    private static final ThreadLocal<WideEvent> CURRENT = new ThreadLocal<>();
    private static final String INTERNAL_EXCEPTION = "Internal Exception: ";
    // "org.postgresql.util.PSQLException: ERROR: " in front of the actual text
    private static final String DRIVER_PREFIX = "^[\\w.$]+Exception: (ERROR: )?";

    private final Map<String, Object> fields = new LinkedHashMap<>();
    private final Map<String, Map<String, Object>> groups = new HashMap<>();
    private boolean failed;

    static WideEvent start() {
        var event = new WideEvent();
        CURRENT.set(event);
        return event;
    }

    static void clear() {
        CURRENT.remove();
    }

    /** Never null: outside a request (tests, startup) enrichment goes to a throwaway event. */
    public static WideEvent current() {
        var event = CURRENT.get();
        return Objects.nonNull(event) ? event : new WideEvent();
    }

    public WideEvent put(String key, Object value) {
        fields.put(key, value);
        return this;
    }

    /**
     * Adds {@code value} under a nested group, e.g. {@code put("payment", "provider", "stripe")}.
     */
    public void put(String group, String key, Object value) {
        groups.computeIfAbsent(group, _ -> {
            var nested = new LinkedHashMap<String, Object>();
            fields.put(group, nested);
            return nested;
        }).put(key, value);
    }

    public void error(Throwable t) {
        failed = true;
        var message = t.getLocalizedMessage();
        if (Objects.isNull(message)) {
            put("message", null);
            return;
        }

        // EclipseLink embeds the statement in the message: "Call: <sql>", "bind => [...]" and
        // "Query: <query>(... sql=...)" (a duplicate of the call, dropped)
        String sql = null;
        String cause = null;
        List<String> params = null;
        var rest = new StringBuilder();
        for (var line : message.strip().split("\n")) {
            var trimmed = line.strip();
            if (line.startsWith(INTERNAL_EXCEPTION)) {
                cause = line.substring(INTERNAL_EXCEPTION.length()).strip().replaceFirst(DRIVER_PREFIX, "");
            }
            else if (line.startsWith("Call: ")) {
                sql = line.substring("Call: ".length()).strip();
            }
            else if (trimmed.startsWith("bind =>")) {
                params = bindValues(trimmed.substring("bind =>".length()));
            }
            else if (!line.startsWith("Query: ")) {
                if (!rest.isEmpty()) {
                    rest.append('\n');
                }
                rest.append(line.stripTrailing());
            }
        }

        // a database failure: keep just the driver's text, without EclipseLink's header, Detail (row values) and Error Code
        put("message", Objects.nonNull(cause) ? cause : rest.toString());
        if (sql != null) {
            put("sql", sql);
            if (Boolean.parseBoolean(PropertiesConfig.get("logging.sql.show-params"))) {
                put("params", Objects.nonNull(params) ? List.of() : params);
            }
        }
    }

    /** "[1, abc]" gives the values; with parameter logging off EclipseLink prints "[2 parameters bound]", so none. */
    private static List<String> bindValues(String bind) {
        var inner = bind.strip();
        if (inner.startsWith("[") && inner.endsWith("]")) {
            inner = inner.substring(1, inner.length() - 1).strip();
        }
        if (inner.isEmpty() || inner.matches("\\d+ parameters? bound")) {
            return List.of();
        }
        return List.of(inner.split(",\\s*"));
    }

    boolean hasError() {
        return failed;
    }

    Object get() {
        return fields.get("request_id");
    }

    Map<String, Object> fields() {
        return fields;
    }
}
