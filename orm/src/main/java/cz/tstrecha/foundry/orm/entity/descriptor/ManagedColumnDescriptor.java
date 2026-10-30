package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.entity.descriptor.code.ColumnFieldDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.code.EntityDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.db.ColumnDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.db.SequenceDescriptor;

public record ManagedColumnDescriptor<E>(
        ColumnDescriptor column,
        ColumnFieldDescriptor field,
        ColumnPathDescriptor<E> entityPath,
        SequenceDescriptor sequenceDescriptor) {

    public ManagedColumnDescriptor(EntityDescriptor<?> entityDescriptor,
                                   ColumnFieldDescriptor field,
                                   ColumnPathDescriptor<E> entityPath,
                                   SequenceDescriptor sequenceDescriptor) {
        var columnDescriptor = entityDescriptor.tableDescriptor().columns().get(field.columnName());

        this(columnDescriptor, field, entityPath, sequenceDescriptor);
    }

}
