package krilovs.andrejs.framework.config.logging;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Decides after the request completes whether its event is kept:
 * always errors and slow requests, a small random share of the rest.
 */
record TailSampler(long slowThresholdMs, double sampleRate) {

    static TailSampler fromEnv() {
        return new TailSampler(
                Long.parseLong(env("LOG_SLOW_MS", "1000")),
                Double.parseDouble(env("LOG_SAMPLE_RATE", "0.05"))
        );
    }

    boolean keep(int status, long durationMs, boolean hasError) {
        return hasError
                || status >= 400
                || durationMs >= slowThresholdMs
                || ThreadLocalRandom.current().nextDouble() < sampleRate;
    }

    private static String env(String name, String fallback) {
        var value = System.getenv(name);
        return Objects.isNull(value) || value.isBlank() ? fallback : value;
    }
}
