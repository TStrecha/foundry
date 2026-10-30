package cz.tstrecha.foundry.orm.entity.persister;

import cz.tstrecha.foundry.orm.entity.descriptor.db.SequenceDescriptor;
import cz.tstrecha.foundry.orm.operation.sql.SequenceNextValueQuery;
import lombok.SneakyThrows;

public class SequencePersister {

    private final String sequenceName;

    public SequencePersister(SequenceDescriptor sequenceDescriptor) {
        this.sequenceName = sequenceDescriptor.sequenceName();
    }

    @SneakyThrows
    public SequenceNextValueQuery createSequenceQuery() {
        return new SequenceNextValueQuery(sequenceName);
    }
}
