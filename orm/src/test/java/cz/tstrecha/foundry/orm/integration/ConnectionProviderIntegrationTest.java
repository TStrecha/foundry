package cz.tstrecha.foundry.orm.integration;

import cz.tstrecha.foundry.orm.connection.provider.ConnectionProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectionProviderIntegrationTest extends AbstractPostgresIntegrationTest {

    @Test
    void realConnectionIsReusedAcrossAcquireCalls() throws Exception {
        var provider = new ConnectionProvider(dataSource());
        var c1 = provider.acquireConnection();
        var c2 = provider.acquireConnection();
        assertThat(c1).isSameAs(c2);
        provider.close();
    }

    @Test
    void connectionIsClosedAfterCloseCall() throws Exception {
        var provider = new ConnectionProvider(dataSource());
        var c = provider.acquireConnection();
        provider.close();
        assertThat(c.isClosed()).isTrue();
    }
}
