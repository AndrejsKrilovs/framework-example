
package krilovs.andrejs.framework.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;

@Slf4j
@RequiredArgsConstructor
public final class ApplicationLifecycle implements AutoCloseable {
    private final Server server;
    private final EntityManagerFactory entityManagerFactory;

    @Override
    public synchronized void close() {
        try {
            if (server.isStarted() || server.isStarting()) {
                server.stop();
            }
        } catch (Exception e) {
            log.error("Failed to stop Jetty", e);
        }

        try {
            if (entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
            }
        } catch (Exception e) {
            log.error("Failed to close JPA", e);
        }
    }
}
