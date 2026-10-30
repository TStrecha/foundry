package cz.tstrecha.foundry.orm.entity.descriptor.db;

import cz.tstrecha.foundry.orm.entity.descriptor.code.ColumnFieldDescriptor;
import jakarta.persistence.Entity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record TableDescriptor(
        String tableName,
        Map<String, ColumnDescriptor> columns
) {

    public TableDescriptor(Class<?> entityType, List<ColumnFieldDescriptor> columnFieldDescriptors) {
        var tableName = entityType.getAnnotation(Entity.class).name();
        var columns = columnFieldDescriptors.stream()
                .collect(Collectors.toMap(
                        ColumnFieldDescriptor::columnName,
                        ColumnDescriptor::new, (a, b) -> a,
                        LinkedHashMap::new));

        this(tableName, columns);
    }

}
