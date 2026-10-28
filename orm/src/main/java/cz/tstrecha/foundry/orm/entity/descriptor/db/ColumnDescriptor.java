package cz.tstrecha.foundry.orm.entity.descriptor.db;

import java.sql.SQLType;

public record ColumnDescriptor(
        String name,
        SQLType type
) {

}
