package cz.tstrecha.foundry.orm.entity.persister.generator;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface PostInsertValueGenerator<T> extends ValueGenerator<T> {

    ValueConsumer<T> createValueConsumer();

    @FunctionalInterface
    interface ValueConsumer<T> {
        T consume(ResultSet resultSet, int index) throws SQLException;
    }

}
