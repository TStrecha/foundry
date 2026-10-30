package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.entity.descriptor.code.ColumnFieldDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.code.EntityDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.db.ColumnDescriptor;

public record ManagedColumnDescriptor<E>(
        ColumnDescriptor column,
        ColumnFieldDescriptor field,
        ColumnPathDescriptor<E> entityPath) {

    public ManagedColumnDescriptor(EntityDescriptor<?> entityDescriptor,
                                   ColumnFieldDescriptor field,
                                   ColumnPathDescriptor<E> entityPath) {
        var columnDescriptor = entityDescriptor.tableDescriptor().columns().get(field.columnName());

        this(columnDescriptor, field, entityPath);
    }

}
