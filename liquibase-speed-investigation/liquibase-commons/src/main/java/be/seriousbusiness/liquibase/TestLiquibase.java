package be.seriousbusiness.liquibase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLiquibase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestLiquibase.class);

    static void updateDatabase() throws SQLException, LiquibaseException {
        try (final Connection connection = DriverManager.getConnection("jdbc:h2:~/test", "sa", "")) {
            final DatabaseConnection databaseConnection = new JdbcConnection(connection);
            final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(databaseConnection);

            final String path = "liquibase/changelog/databaseChangelog.xml";

            final Liquibase liquibase = new Liquibase(path, new ClassLoaderResourceAccessor(), database);
            liquibase.update("");

            // Log DATABASECHANGELOG:
            try (final ResultSet resultSet = ((JdbcConnection) databaseConnection).prepareCall("select * from DATABASECHANGELOG").executeQuery()) {
                while (resultSet.next()) {
                    final String id = resultSet.getString("ID");
                    final String filename = resultSet.getString("FILENAME");
                    final Timestamp dateExecuted = resultSet.getTimestamp("DATEEXECUTED");
                    final String execType = resultSet.getString("EXECTYPE");
                    final String liquibaseVersion = resultSet.getString("LIQUIBASE");
                    LOGGER.info("Changelog: {} - Changeset: {} - {} - {} - {}", filename, id, dateExecuted, execType, liquibaseVersion);
                }
            }

            // Log number of inserted users:
            try (final ResultSet resultSet = ((JdbcConnection) databaseConnection).prepareCall("select count(*) as users from USER").executeQuery()) {
                while (resultSet.next()) {
                    final Long users = resultSet.getLong("users");
                    LOGGER.info("Found {} user(s)", users);
                }
            }
        }
    }

    static void clearDatabase() {
        try (final Connection connection = DriverManager.getConnection("jdbc:h2:~/test", "sa", "")) {
            connection.prepareCall("drop table DATABASECHANGELOG").execute();
            connection.prepareCall("drop table USER").execute();
        } catch (final SQLException e) {
            LOGGER.debug("Error when clearing the database", e);
        }
    }
}
