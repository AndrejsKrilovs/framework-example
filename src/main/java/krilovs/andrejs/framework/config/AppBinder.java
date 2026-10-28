package krilovs.andrejs.framework.config;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.TransactionManager;
import krilovs.andrejs.framework.config.transaction.RequestEntityManager;
import krilovs.andrejs.framework.config.transaction.TransactionConfig;
import lombok.RequiredArgsConstructor;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

@RequiredArgsConstructor
public class AppBinder extends AbstractBinder {
    private final EntityManagerFactory entityManagerFactory;
    private final ApplicationLifecycle lifecycle;

    @Override
    protected void configure() {
        bind(lifecycle).to(ApplicationLifecycle.class);
        bind(entityManagerFactory).to(EntityManagerFactory.class);
        bind(TransactionConfig.transactionManager()).to(TransactionManager.class);
        bindFactory(RequestEntityManager.class).to(EntityManager.class).in(RequestScoped.class);

        var basePackage = PropertiesConfig.get("app.base_package");

        try (var scan = new ClassGraph()
                .acceptPackages(basePackage)
                .enableAnnotationInfo()
                .scan()) {

            for (ClassInfo info : scan.getClassesWithAnnotation(Singleton.class)) {
                if (info.isInterface() || info.isAbstract() || info.getName().equals(ApplicationLifecycle.class.getName())) {
                    continue;
                }

                var implementation = info.loadClass();
                var builder = bind(implementation).to(implementation);
                for (ClassInfo contract : info.getInterfaces()) {
                    if (contract.getName().startsWith(basePackage)) {
                        builder.to(contract.loadClass());
                    }
                }
                builder.in(Singleton.class);
            }
        }
    }
}
