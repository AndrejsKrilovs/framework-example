package krilovs.andrejs.framework.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Provider
public class ConstraintViolationExceptionResponse implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {
        var messages = new TreeMap<String, List<String>>();

        for (var violation : e.getConstraintViolations()) {
            var field = "";
            for (var node : violation.getPropertyPath()) {
                field = node.getName();
            }
            messages.computeIfAbsent(field, _ -> new ArrayList<>()).add(violation.getMessage());
        }

        // A single message stays a plain string; several become a list.
        var violations = new TreeMap<String, Object>();
        messages.forEach((field, list) -> violations.put(field, list.size() == 1 ? list.getFirst() : list));
        return ValidationFailure.response(violations);
    }
}
