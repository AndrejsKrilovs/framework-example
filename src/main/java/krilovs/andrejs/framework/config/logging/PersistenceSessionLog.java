package krilovs.andrejs.framework.config.logging;

import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Routes EclipseLink's errors into SLF4J, so they go through the same logback appenders and
 * JSON encoder as everything else. Only {@link org.eclipse.persistence.logging.SessionLog#SEVERE} entries are forwarded,
 * as ERROR; everything else (SQL, warnings, info) is dropped.
 * <p>
 * Instantiated by EclipseLink from the class name in eclipselink.logging.logger.
 */
public class PersistenceSessionLog extends AbstractSessionLog {
    private static final String ROOT = "org.eclipse.persistence";

    @Override
    public boolean shouldLog(int level, String category) {
        return level >= SessionLog.OFF && LoggerFactory.getLogger(loggerName(category)).isErrorEnabled();
    }

    @Override
    public boolean shouldLog(int level) {
        return shouldLog(level, null);
    }

    @Override
    public void log(SessionLogEntry entry) {
        if (!shouldLog(entry.getLevel(), entry.getNameSpace())) {
            return;
        }

        var logger = LoggerFactory.getLogger(loggerName(entry.getNameSpace()));
        var message = formatMessage(entry);
        if (Objects.nonNull(entry.getException())) {
            logger.error(message, entry.getException());
        }
        else {
            logger.error(message);
        }
    }

    private static String loggerName(String category) {
        return Objects.isNull(category) || category.isBlank() ? ROOT : "%s.%s".formatted(ROOT, category);
    }
}
