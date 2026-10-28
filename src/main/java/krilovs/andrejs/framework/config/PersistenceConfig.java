package krilovs.andrejs.framework.config;

import io.github.classgraph.ClassGraph;
import jakarta.persistence.Converter;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import krilovs.andrejs.framework.config.logging.PersistenceSessionLog;
import krilovs.andrejs.framework.config.transaction.NarayanaTransactionController;
import lombok.experimental.UtilityClass;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.eclipse.persistence.logging.SessionLog;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;
import javax.sql.DataSource;

/**
 * Builds the EclipseLink EntityManagerFactory in code (no persistence.xml).
 * Managed classes are discovered the same way AppBinder finds singletons: by scanning
 * app.base_package for @Entity, @MappedSuperclass, @Embeddable and @Converter.
 * <p>
 * JTA unit: transactions come from Narayana (see TransactionFilter). The pool is Agroal, which enlists
 * its connections in the current transaction by itself, so one DataSource serves both roles.
 * <p>
 * Connects to the database on creation, so call it after {@link krilovs.andrejs.framework.config.PropertiesConfig#load()}.
 */
@UtilityClass
public class PersistenceConfig {
    private static final String UNIT_NAME = "application_persistence_unit";

    public static EntityManagerFactory createEntityManagerFactory(DataSource dataSource) {
        var properties = Map.of(
                PersistenceUnitProperties.WEAVING, "true",
                PersistenceUnitProperties.LOGGING_LOGGER, PersistenceSessionLog.class.getName(),
                PersistenceUnitProperties.LOGGING_LEVEL, SessionLog.ALL_LABEL,
                PersistenceUnitProperties.LOGGING_PARAMETERS, PropertiesConfig.get("logging.sql.show-params"),
                PersistenceUnitProperties.TARGET_SERVER, NarayanaTransactionController.class.getName()
        );

        return new PersistenceProvider().createContainerEntityManagerFactory(
                new Unit(managedClassNames(), dataSource),
                properties
        );
    }

    private static List<String> managedClassNames() {
        try (var scan = new ClassGraph()
                .acceptPackages(PropertiesConfig.get("app.base_package"))
                .enableAnnotationInfo()
                .scan()) {
            return Stream.of(Entity.class, MappedSuperclass.class, Embeddable.class, Converter.class)
                    .flatMap(annotation -> scan.getClassesWithAnnotation(annotation).getNames().stream())
                    .distinct()
                    .toList();
        }
    }

    /** Minimal programmatic equivalent of a persistence.xml unit. */
    private record Unit(List<String> managedClassNames, DataSource dataSource) implements PersistenceUnitInfo {
        @Override
        public String getPersistenceUnitName() {
            return UNIT_NAME;
        }

        @Override
        public String getPersistenceProviderClassName() {
            return PersistenceProvider.class.getName();
        }

        @Override
        public PersistenceUnitTransactionType getTransactionType() {
            return PersistenceUnitTransactionType.JTA;
        }

        @Override
        public DataSource getJtaDataSource() {
            return dataSource;
        }

        @Override
        public DataSource getNonJtaDataSource() {
            return dataSource;
        }

        @Override
        public List<String> getMappingFileNames() {
            return List.of();
        }

        @Override
        public List<URL> getJarFileUrls() {
            return List.of();
        }

        @Override
        public URL getPersistenceUnitRootUrl() {
            return PersistenceConfig.class.getProtectionDomain().getCodeSource().getLocation();
        }

        @Override
        public List<String> getManagedClassNames() {
            return managedClassNames;
        }

        @Override
        public boolean excludeUnlistedClasses() {
            return true;
        }

        @Override
        public SharedCacheMode getSharedCacheMode() {
            return SharedCacheMode.UNSPECIFIED;
        }

        @Override
        public ValidationMode getValidationMode() {
            return ValidationMode.AUTO;
        }

        @Override
        public Properties getProperties() {
            return new Properties();
        }

        @Override
        public String getPersistenceXMLSchemaVersion() {
            return "3.1";
        }

        @Override
        public ClassLoader getClassLoader() {
            return Thread.currentThread().getContextClassLoader();
        }

        @Override
        public void addTransformer(ClassTransformer transformer) {
            // weaving is disabled
        }

        @Override
        public ClassLoader getNewTempClassLoader() {
            return getClassLoader();
        }
    }
}
