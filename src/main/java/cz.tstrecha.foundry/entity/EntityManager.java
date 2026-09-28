package cz.tstrecha.foundry.entity;

import cz.tstrecha.foundry.connection.DBSession;
import cz.tstrecha.foundry.connection.FoundryContext;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class EntityManager {

    private final DBSession session;

    public <T, ID> T find(Class<T> entityType, ID id) {
        var tableDefinition = FoundryContext.getEntityDefinitions().get(entityType);
        var filterClause = String.format("%s = '%s'", tableDefinition.idColumnDefinition().name(), id);
        var columns = tableDefinition.columns().values().stream().map(ColumnDefinition::name).collect(Collectors.joining(","));

        var result = session.selectOne(tableDefinition.tableName(), filterClause, columns);

        return EntityParser.parse(entityType, result.columnLabels(), result.row(), tableDefinition);
    }

    public <T> List<T> findAll(Class<T> entityType) {
        var tableDefinition = FoundryContext.getEntityDefinitions().get(entityType);
        var columns = tableDefinition.columns().values().stream().map(ColumnDefinition::name).collect(Collectors.joining(","));

        var result = session.selectAll(tableDefinition.tableName(), null, columns);

        return result.rows().stream()
                .map(row -> EntityParser.parse(entityType, result.columnLabels(), row, tableDefinition))
                .toList();
    }

}
