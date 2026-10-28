import krilovs.andrejs.framework.config.AppControllerConfig;
import krilovs.andrejs.framework.config.ApplicationLifecycle;
import krilovs.andrejs.framework.config.PropertiesConfig;
import krilovs.andrejs.framework.config.DataSourceConfig;
import krilovs.andrejs.framework.config.DatabaseMigration;
import krilovs.andrejs.framework.config.ObjectMapperProvider;
import krilovs.andrejs.framework.config.PersistenceConfig;
import jakarta.servlet.DispatcherType;
import krilovs.andrejs.framework.config.logging.WideEventFilter;
import org.eclipse.jetty.ee11.servlet.FilterHolder;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.EnumSet;

void main() throws Exception {
    // first thing: logback and the date formatters read their settings from here
    PropertiesConfig.load();

    // schema first: JPA expects the tables to exist
    try (var dataSource = DataSourceConfig.createDataSource()) {
        DatabaseMigration.migrate(dataSource);

        var server = new Server(PropertiesConfig.getInt("server.port"));
        var entityManagerFactory = PersistenceConfig.createEntityManagerFactory(dataSource);
        try (var lifecycle = new ApplicationLifecycle(server, entityManagerFactory)) {
            Runtime.getRuntime().addShutdownHook(new Thread(lifecycle::close, "application-shutdown"));

            // one ObjectMapper shared by Jersey and the servlet filter (which lives outside HK2)
            var objectMapperProvider = new ObjectMapperProvider();
            var resourceConfig = new AppControllerConfig(entityManagerFactory, lifecycle, objectMapperProvider);

            var context = new ServletContextHandler();
            context.setContextPath("/");
            context.addFilter(
                    new FilterHolder(new WideEventFilter(objectMapperProvider.getContext(null))),
                    "/*",
                    EnumSet.of(DispatcherType.REQUEST)
            );
            context.addServlet(
                    new ServletHolder(new ServletContainer(resourceConfig)),
                    "/*"
            );

            server.setHandler(context);
            server.start();
            server.join();
        }
    }
}
