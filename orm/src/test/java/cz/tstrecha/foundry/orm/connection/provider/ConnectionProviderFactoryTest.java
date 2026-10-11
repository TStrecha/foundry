package cz.tstrecha.foundry.orm.connection.provider;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ConnectionProviderFactoryTest {

    @Test
    void createsIndependentProviderInstances() {
        var ds = mock(DataSource.class);
        var factory = new ConnectionProviderFactory(ds);

        var a = factory.createConnectionProvider();
        var b = factory.createConnectionProvider();

        assertThat(a).isNotNull();
        assertThat(b).isNotNull();
        assertThat(a).isNotSameAs(b);
    }
}
