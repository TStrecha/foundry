package cz.tstrecha.foundry.entity;

import java.util.Map;

public record EntityDefinition(Class<?> entity, String tableName, ColumnDefinition idColumnDefinition, Map<String, ColumnDefinition> columns) {
}
