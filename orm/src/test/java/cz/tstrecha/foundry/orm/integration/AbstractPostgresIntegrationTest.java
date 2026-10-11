package cz.tstrecha.foundry.orm.integration;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base class for integration tests that need a real PostgreSQL server. Uses Zonky's
 * embedded postgres so the tests don't require Docker.
 */
public abstract class AbstractPostgresIntegrationTest {

    protected static EmbeddedPostgres postgres;
    protected static String jdbcUrl;
    protected static String user;
    protected static String password;

    @BeforeAll
    static void startPostgres() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        int port = postgres.getPort();
        jdbcUrl = "jdbc:postgresql://localhost:" + port + "/postgres";
        user = "postgres";
        password = "postgres";
    }

    @AfterAll
    static void stopPostgres() throws Exception {
        if (postgres != null) {
            postgres.close();
        }
    }

    protected static DataSource dataSource() {
        return postgres.getPostgresDatabase();
    }

    protected static void execute(String sql) throws SQLException {
        try (Connection c = dataSource().getConnection();
             var stmt = c.createStatement()) {
            stmt.execute(sql);
        }
    }
}
