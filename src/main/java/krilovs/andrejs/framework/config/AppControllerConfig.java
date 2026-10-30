package krilovs.andrejs.framework.config;

import io.github.classgraph.ClassGraph;
import jakarta.persistence.EntityManagerFactory;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

/**
 * Jersey configuration. REST resources (@Path) and providers such as exception mappers (@Provider)
 * are discovered by scanning app.base_package, the same way AppBinder finds singletons,
 * so a new controller or mapper needs no registration.
 */
public class AppControllerConfig extends ResourceConfig {
    // outside app.base_package, but holds providers that must be registered: exception mappers, TransactionFeature
    private static final String FRAMEWORK_PACKAGE = "krilovs.andrejs.framework";

    public AppControllerConfig(EntityManagerFactory entityManagerFactory,
                               ApplicationLifecycle lifecycle,
                               ObjectMapperProvider objectMapperProvider) {
        property(ServerProperties.WADL_FEATURE_DISABLE, true);
        register(new AppBinder(entityManagerFactory, lifecycle));
        // registered as an instance: it is shared with the servlet filter, which lives outside HK2
        register(objectMapperProvider);
        register(RolesAllowedDynamicFeature.class);

        try (var scan = new ClassGraph()
                .acceptPackages(PropertiesConfig.get("app.base_package"), FRAMEWORK_PACKAGE)
                .enableAnnotationInfo()
                .scan()) {
            var resources = scan.getClassesWithAnnotation(Path.class);
            var providers = scan.getClassesWithAnnotation(Provider.class);

            resources.union(providers).stream()
                    .filter(info -> !info.isInterface() && !info.isAbstract())
                    .filter(info -> !info.getName().equals(ObjectMapperProvider.class.getName()))
                    .forEach(info -> register(info.loadClass()));
        }
    }
}
