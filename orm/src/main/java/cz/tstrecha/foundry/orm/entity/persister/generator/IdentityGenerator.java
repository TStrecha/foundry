package cz.tstrecha.foundry.orm.entity.persister.generator;

import java.sql.ResultSet;

public class IdentityGenerator implements PostInsertValueGenerator<Long> {

    @Override
    public ValueConsumer<Long> createValueConsumer() {
        return ResultSet::getLong;
    }

}
