package cz.tstrecha.foundry.orm.entity;

import cz.tstrecha.foundry.orm.connection.provider.ConnectionProviderFactory;
import cz.tstrecha.foundry.orm.entity.registry.PersisterRegistry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EntityManagerFactory {

    private final PersisterRegistry persisterRegistry;
    private final ConnectionProviderFactory connectionProviderFactory;

    public EntityManager createEntityManager() {
        var connectionProvider = connectionProviderFactory.createConnectionProvider();
        return new EntityManager(connectionProvider, persisterRegistry);
    }
}
