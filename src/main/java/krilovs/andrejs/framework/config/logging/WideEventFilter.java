package krilovs.andrejs.framework.config.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import krilovs.andrejs.framework.config.PropertiesConfig;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Emits one wide event per request, including on failure.
 * Deliberately logs no headers, query string or bodies: this is a payment service.
 */
@RequiredArgsConstructor
public class WideEventFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger("wide_event");
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern(PropertiesConfig.get("app.date_time")).withZone(ZoneId.systemDefault());
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private final TailSampler sampler = TailSampler.fromEnv();
    private final String service = env("SERVICE_NAME", "payment-service");
    private final String version = env("SERVICE_VERSION", "dev");
    private final String region = env("REGION", "local");
    private final ObjectMapper mapper;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        var request = (HttpServletRequest) req;
        var response = (HttpServletResponse) resp;

        var requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        response.setHeader(REQUEST_ID_HEADER, requestId);

        var event = WideEvent.start()
                .put("timestamp", TIMESTAMP_FORMAT.format(Instant.now()))
                .put("request_id", requestId)
                .put("service", service)
                .put("version", version)
                .put("region", region)
                .put("method", request.getMethod())
                .put("path", request.getRequestURI())
                .put("user_agent", request.getHeader("User-Agent"));

        var start = System.nanoTime();
        try {
            chain.doFilter(req, resp);
        }
        catch (IOException | ServletException | RuntimeException e) {
            event.error(e);
            throw e;
        }
        finally {
            emit(event, response, (System.nanoTime() - start) / 1_000_000);
            WideEvent.clear();
        }
    }

    private void emit(WideEvent event, HttpServletResponse response, long durationMs) {
        var hasError = event.hasError();
        var status = response.getStatus();
        if (hasError && status < HttpStatus.BAD_REQUEST_400) {
            status = HttpStatus.INTERNAL_SERVER_ERROR_500; // exception escaped before a response status was set
        }

        event.put("status", status)
                .put("duration_ms", durationMs)
                .put("outcome", status >= HttpStatus.BAD_REQUEST_400 ? "error" : "success");

        if (!sampler.keep(status, durationMs, hasError)) {
            return;
        }

        var line = serialize(event);
        if (status >= HttpStatus.INTERNAL_SERVER_ERROR_500) {
            LOG.error(line);
        }
        else if (status >= HttpStatus.BAD_REQUEST_400) {
            LOG.warn(line);
        }
        else {
            LOG.info(line);
        }
    }

    private String serialize(WideEvent event) {
        try {
            return mapper.writeValueAsString(event.fields());
        }
        catch (JsonProcessingException e) {
            return "{\"request_id\":\"" + event.get() + "\",\"log_error\":\"serialization failed\"}";
        }
    }

    private static String env(String name, String fallback) {
        var value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
