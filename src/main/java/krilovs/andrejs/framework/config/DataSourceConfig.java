package krilovs.andrejs.framework.config;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.agroal.narayana.NarayanaTransactionIntegration;
import krilovs.andrejs.framework.config.transaction.TransactionConfig;
import lombok.experimental.UtilityClass;

import java.sql.SQLException;
import java.time.Duration;

/**
 * The connection pool. Agroal is integrated with Narayana: inside a JTA transaction getConnection()
 * returns the connection enlisted in it (committed or rolled back with the transaction),
 * outside one it is an ordinary pooled connection.
 */
@UtilityClass
public class DataSourceConfig {
    public static AgroalDataSource createDataSource() {
        var configuration = new AgroalDataSourceConfigurationSupplier()
                .connectionPoolConfiguration(pool -> pool
                        .minSize(PropertiesConfig.getInt("database.pool.minimum_idle"))
                        .maxSize(PropertiesConfig.getInt("database.pool.maximum_size"))
                        .acquisitionTimeout(millis("database.pool.connection_timeout"))
                        .reapTimeout(millis("database.pool.idle_timeout"))
                        .maxLifetime(millis("database.pool.max_lifetime"))
                        .transactionIntegration(new NarayanaTransactionIntegration(
                                TransactionConfig.transactionManager(),
                                TransactionConfig.transactionSynchronizationRegistry()))
                        .connectionFactoryConfiguration(factory -> factory
                                .connectionProviderClassName(PropertiesConfig.get("database.driver"))
                                .jdbcUrl(PropertiesConfig.get("database.url"))
                                .principal(new NamePrincipal(PropertiesConfig.get("database.username")))
                                .credential(new SimplePassword(PropertiesConfig.get("database.password")))));
        try {
            return AgroalDataSource.from(configuration);
        }
        catch (SQLException e) {
            throw new IllegalStateException("Cannot create the connection pool", e);
        }
    }

    private static Duration millis(String key) {
        return Duration.ofMillis(PropertiesConfig.getInt(key));
    }
}
