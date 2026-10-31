package cz.tstrecha.foundry.orm.entity.persister.generator;

import cz.tstrecha.foundry.orm.connection.DBSession;

import java.util.UUID;
import java.util.function.Function;

public class UUIDGenerator implements PreInsertValueGenerator<UUID> {

    @Override
    public Function<DBSession, UUID> createValueProvider() {
        return _ -> UUID.randomUUID();
    }
}
