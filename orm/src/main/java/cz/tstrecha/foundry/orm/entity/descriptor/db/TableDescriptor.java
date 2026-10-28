package cz.tstrecha.foundry.orm.entity.descriptor.db;

import java.util.List;

public record TableDescriptor(
        String name,
        List<ColumnDescriptor> columns
) {

}
