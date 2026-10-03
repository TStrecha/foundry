package cz.tstrecha.foundry.orm.connection.provider;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RequiredArgsConstructor
public class ConnectionProvider implements AutoCloseable {

    private final DataSource dataSource;
    private Connection connection;

    public Connection acquireConnection() throws SQLException {
        if(connection == null) {
            connection = dataSource.getConnection();
        }

        return connection;
    }

    public void close() throws SQLException {
        if(connection != null) {
            connection.close();
        }
    }

}
