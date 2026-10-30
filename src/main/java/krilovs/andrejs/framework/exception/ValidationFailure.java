package krilovs.andrejs.framework.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import krilovs.andrejs.framework.config.logging.WideEvent;
import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * The 400 response and wide event shared by the mappers of request validation failures.
 * Messages must be static text: they are logged, and this is a payment service, so never the rejected value.
 */
@UtilityClass
final class ValidationFailure {
    static Response response(Map<String, Object> violations) {
        var event = WideEvent.current();
        violations.forEach((field, message) -> event.put("details", field, message));

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(ApiExceptionResponse.validation("Validation failed", violations))
                .build();
    }
}
