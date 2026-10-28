package krilovs.andrejs.framework.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import krilovs.andrejs.framework.config.logging.WideEvent;

@Provider
public class ApiExceptionController implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException e) {
        // Mapped exceptions never reach the servlet filter, so record them on the wide event here.
        WideEvent.current().error(e);

        return switch (e) {
            case WebApplicationException wae -> buildResponse(wae.getResponse().getStatusInfo(), wae.getMessage());
            case IllegalStateException ex -> buildResponse(Response.Status.CONFLICT, ex.getMessage());
            case IllegalArgumentException ex -> buildResponse(Response.Status.BAD_REQUEST, ex.getMessage());
            default -> buildResponse(Response.Status.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred");
        };
    }

    private Response buildResponse(Response.StatusType status, String message) {
        var body = ApiExceptionResponse.of(status, message);
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(body)
                .build();
    }
}
