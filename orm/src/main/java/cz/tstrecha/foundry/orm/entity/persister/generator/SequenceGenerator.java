package cz.tstrecha.foundry.orm.entity.persister.generator;

import cz.tstrecha.foundry.orm.connection.DBSession;
import cz.tstrecha.foundry.orm.entity.descriptor.db.SequenceDescriptor;
import cz.tstrecha.foundry.orm.operation.sql.SequenceNextValueQuery;

import java.util.function.Function;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

public class SequenceGenerator implements ValueGenerator<Long> {

    private final String sequenceName;

    public SequenceGenerator(SequenceDescriptor sequenceDescriptor) {
        requireNotNullArgument(sequenceDescriptor, "sequenceDescriptor");

        this.sequenceName = sequenceDescriptor.sequenceName();
    }

    @Override
    public Function<DBSession, Long> createValueProvider() {
        return session -> session.selectNextValueFromSequence(new SequenceNextValueQuery(sequenceName));
    }
}
