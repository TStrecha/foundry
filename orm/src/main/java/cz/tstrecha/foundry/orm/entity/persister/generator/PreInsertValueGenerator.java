package cz.tstrecha.foundry.orm.entity.persister.generator;

import cz.tstrecha.foundry.orm.connection.DBSession;

import java.util.function.Function;

public interface PreInsertValueGenerator<T> extends ValueGenerator<T> {

    Function<DBSession, T> createValueProvider();

}
