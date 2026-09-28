package cz.tstrecha.foundry.orm.entity;

import java.util.Map;

public record EntityDefinition<T>(Class<T> entity, String tableName, ColumnDefinition idColumnDefinition, Map<String, ColumnDefinition> columns) {
}
