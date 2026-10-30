package krilovs.andrejs.framework.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;
import java.util.Objects;

/**
 * A request body value that Jackson cannot convert (e.g. currency "EEE").
 * Registered for exactly JsonMappingException so it wins over Jersey's built-in mapper of the same type.
 * The rejected value is not logged: this is a payment service.
 */
@Provider
public class JsonMappingExceptionResponse implements ExceptionMapper<JsonMappingException> {

    @Override
    public Response toResponse(JsonMappingException e) {
        var field = e.getPath().isEmpty() ? "body" : lastFieldName(e);
        var type = e instanceof InvalidFormatException format && Objects.nonNull(format.getTargetType())
                ? format.getTargetType().getSimpleName()
                : null;
        var message = Objects.isNull(type) ? "Invalid value" : "Invalid %s value".formatted(type);
        return ValidationFailure.response(Map.of(field, message));
    }

    private static String lastFieldName(JsonMappingException e) {
        var path = e.getPath();
        var name = path.getLast().getFieldName();
        return Objects.isNull(name) ? "body" : name;
    }
}
