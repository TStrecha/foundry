package cz.tstrecha.foundry.orm;

import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.connection.provider.ConnectionProviderFactory;
import cz.tstrecha.foundry.orm.entity.EntityManager;
import cz.tstrecha.foundry.orm.entity.EntityManagerFactory;
import cz.tstrecha.foundry.orm.entity.registry.PersisterRegistry;
import lombok.SneakyThrows;
import lombok.ToString;
import org.postgresql.ds.PGSimpleDataSource;

@ToString
public class FoundryContext {

    private final ConnectionProviderFactory connectionProviderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final PersisterRegistry persisterRegistry;

    @SneakyThrows
    public FoundryContext(FoundryConfiguration foundryConfiguration) {
        this.persisterRegistry = new PersisterRegistry(foundryConfiguration.scanningRoots());

        var dataSource = new PGSimpleDataSource();
        dataSource.setURL(foundryConfiguration.url());
        dataSource.setUser(foundryConfiguration.user());
        dataSource.setPassword(foundryConfiguration.password());

        this.connectionProviderFactory = new ConnectionProviderFactory(dataSource);
        this.entityManagerFactory = new EntityManagerFactory(persisterRegistry, connectionProviderFactory);
    }

    public EntityManager openSession() {
        return this.entityManagerFactory.createEntityManager();
    }
}
