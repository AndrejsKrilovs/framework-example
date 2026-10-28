package krilovs.andrejs.framework.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.ws.rs.core.Response.StatusType;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiExceptionResponse(
        Integer status,
        String error,
        String message,
        Instant timestamp,
        Map<String, Object> violations
) {
    public static ApiExceptionResponse of(StatusType status, String message) {
        return new ApiExceptionResponse(
                status.getStatusCode(),
                status.getReasonPhrase(),
                message,
                Instant.now(),
                null
        );
    }

    public static ApiExceptionResponse validation(String message, Map<String, Object> violations) {
        return new ApiExceptionResponse(
                400,
                "Bad Request",
                message,
                Instant.now(),
                violations
        );
    }
}