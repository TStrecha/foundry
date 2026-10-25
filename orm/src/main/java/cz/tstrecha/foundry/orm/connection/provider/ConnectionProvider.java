package cz.tstrecha.foundry.orm.connection.provider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

public class ConnectionProvider implements AutoCloseable {

    private final DataSource dataSource;
    private Connection connection;

    public ConnectionProvider(DataSource dataSource) {
        requireNotNullArgument(dataSource, "dataSource");
        this.dataSource = dataSource;
    }

    public Connection acquireConnection() throws SQLException {
        if(connection == null) {
            connection = dataSource.getConnection();
        }

        return connection;
    }

    public void close() throws SQLException {
        if(connection != null) {
            connection.close();
            connection = null;
        }
    }

    public boolean isConnected() {
        return connection != null;
    }
}
