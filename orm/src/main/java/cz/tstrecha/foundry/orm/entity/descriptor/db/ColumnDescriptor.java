package cz.tstrecha.foundry.orm.entity.descriptor.db;

import cz.tstrecha.foundry.orm.entity.descriptor.code.ColumnFieldDescriptor;
import cz.tstrecha.foundry.orm.entity.type.TypeHandlerResolver;

import java.sql.SQLType;

public record ColumnDescriptor(
        String name,
        SQLType sqlType
) {

    public ColumnDescriptor(ColumnFieldDescriptor columnFieldDescriptor) {
        var sqlType = TypeHandlerResolver.findHandlerForType(columnFieldDescriptor.type(), columnFieldDescriptor.enumType()).getSqlType();

        this(columnFieldDescriptor.columnName(), sqlType);
    }
}
