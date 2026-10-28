package krilovs.andrejs.framework.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Reads application.yml into system properties under their dotted path (server.port,
 * logging.file.directory), so logback.xml can resolve ${...} from there too.
 * Properties already set with -D win over the file.
 * <p>
 * {@link #load()} must run before the first logger is created.
 */
@UtilityClass
public class PropertiesConfig {
    private static final String RESOURCE = "application.yml";

    public static void load() {
        try (var in = PropertiesConfig.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (Objects.isNull(in)) {
                throw new IllegalStateException("%s not found on classpath".formatted(RESOURCE));
            }
            publish("", new YAMLMapper().readTree(in));
        }
        catch (IOException e) {
            throw new UncheckedIOException("Failed to read %s".formatted(RESOURCE), e);
        }
    }

    public static String get(String key) {
        var value = System.getProperty(key);
        if (Objects.isNull(value)) {
            throw new IllegalStateException("Missing config property: %s".formatted(key));
        }
        return value;
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    private static void publish(String prefix, JsonNode node) {
        if (node.isObject()) {
            node.properties().forEach(e ->
                    publish(prefix.isEmpty() ? e.getKey() : "%s.%s".formatted(prefix, e.getKey()), e.getValue())
            );
        }
        else if (node.isValueNode() && !node.isNull()) {
            System.getProperties().putIfAbsent(prefix, node.asText());
        }
    }
}
