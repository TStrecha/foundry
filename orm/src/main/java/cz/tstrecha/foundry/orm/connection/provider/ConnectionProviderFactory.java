package cz.tstrecha.foundry.orm.connection.provider;

import javax.sql.DataSource;

public class ConnectionProviderFactory {

    private final DataSource dataSource;

    public ConnectionProviderFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public ConnectionProvider createConnectionProvider() {
        return new ConnectionProvider(dataSource);
    }
}
