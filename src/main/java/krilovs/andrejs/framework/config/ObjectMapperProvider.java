package krilovs.andrejs.framework.config;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;
import java.util.TimeZone;

/**
 * Jackson configuration used by Jersey: registers java.time support and renders dates
 * in the JVM default time zone.
 */
@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return JsonMapper.builder()
                .findAndAddModules()
                .defaultTimeZone(TimeZone.getDefault())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .withConfigOverride(Instant.class, override -> override.setFormat(
                        JsonFormat.Value.forShape(JsonFormat.Shape.STRING).withPattern(PropertiesConfig.get("app.date_time"))))
                .build();
    }
}
