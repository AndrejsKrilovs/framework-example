package krilovs.andrejs.framework.config.logging;

import ch.qos.logback.classic.encoder.JsonEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextVO;
import krilovs.andrejs.framework.config.PropertiesConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Setter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * {@link ch.qos.logback.classic.encoder.JsonEncoder} that replaces the numeric timestamp/nanoseconds/sequenceNumber triple
 * with a single leading "timestamp" field, e.g. "2026-10-21 14:03:07".
 * With {@code <pretty>true</pretty>} the output is indented, multi-line JSON instead of one line per event.
 */
@Setter
public class ReadableTimeJsonEncoder extends JsonEncoder {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern(PropertiesConfig.get("app.date_time")).withZone(ZoneId.systemDefault());

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectWriter PRETTY = MAPPER.writerWithDefaultPrettyPrinter();

    private static final String MESSAGE_FIELD = "formattedMessage";

    private boolean pretty;

    public ReadableTimeJsonEncoder() {
        setWithSequenceNumber(false);
        setWithTimestamp(false);
        setWithNanoseconds(false);
        setWithMessage(false);
        setWithFormattedMessage(true);
        setWithArguments(false);
        setWithThrowable(false);
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        byte[] json = super.encode(event);
        // json is "{...}\n" and always has at least the level field, so the prefix ends with a comma
        byte[] prefix = ("{\"date\":\"" + FORMAT.format(event.getInstant()) + "\",").getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[prefix.length + json.length - 1];
        System.arraycopy(prefix, 0, out, 0, prefix.length);
        System.arraycopy(json, 1, out, prefix.length, json.length - 1);
        return pretty ? prettyPrint(out) : out;
    }

    @Override
    protected void appendLoggerContext(StringBuilder sb, LoggerContextVO context) {
        if (context == null) {
            super.appendLoggerContext(sb, null);
            return;
        }
        sb.append("\"context\":{");
        appenderMember(sb, "name", String.valueOf(context.getName()));
        sb.append(',');
        appenderMember(sb, "from", FORMAT.format(Instant.ofEpochMilli(context.getBirthTime())));
        sb.append(',');
        appendMap(sb, "properties", context.getPropertyMap());
        sb.append('}');
    }

    private static byte[] prettyPrint(byte[] json) {
        try {
            var tree = (ObjectNode) MAPPER.readTree(json);
            inlineJsonMessage(tree);
            return "%s %s".formatted(
                    PRETTY.writeValueAsString(tree),
                    System.lineSeparator()
            ).getBytes(StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            return json;
        }
    }

    /** Wide events arrive as a JSON string; embed them as a real object so they are indented too. */
    private static void inlineJsonMessage(ObjectNode tree) {
        var message = tree.get(MESSAGE_FIELD);
        if (message == null || !message.isTextual() || !message.asText().startsWith("{")) {
            return;
        }
        try {
            tree.set(MESSAGE_FIELD, MAPPER.readTree(message.asText()));
        }
        catch (IOException e) {
            // not JSON after all, keep the plain string
        }
    }
}
