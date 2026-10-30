package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.entity.descriptor.code.ColumnFieldDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.db.ColumnDescriptor;

public record ManagedColumnDescriptor<E>(
        ColumnDescriptor column,
        ColumnFieldDescriptor field,
        ColumnPathDescriptor<E> entityPath) {
}
