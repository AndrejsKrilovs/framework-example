package krilovs.andrejs.framework.config;

import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.experimental.UtilityClass;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Applies Liquibase migrations before JPA initialization.
 * Uses the application's shared connection pool.
 */
@UtilityClass
public class DatabaseMigration {
    private static final String CHANGELOG = "db/changelog/db.changelog-master.yaml";

    public static void migrate(DataSource dataSource) {
        try (var connection = dataSource.getConnection()) {
            var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (var liquibase = new Liquibase(CHANGELOG, new ClassLoaderResourceAccessor(), database)) {
                liquibase.update();
            }
        }
        catch (SQLException | LiquibaseException e) {
            throw new IllegalStateException("Database migration failed", e);
        }
    }
}
