package cz.tstrecha.foundry.orm.entity;

import cz.tstrecha.foundry.orm.connection.DBSession;
import cz.tstrecha.foundry.orm.entity.parser.EntityParser;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class EntityManager {

    private final DBSession session;
    private final EntityContext entityContext;
    private final EntityParser entityParser;

    public EntityManager(DBSession session, EntityContext entityContext) {
        this.session = session;
        this.entityContext = entityContext;
        this.entityParser = new EntityParser(entityContext);
    }

    public <T, ID> T find(Class<T> entityType, ID id) {
        var tableDefinition = entityContext.getEntityDefinition(entityType);
        var filterClause = String.format("%s = '%s'", tableDefinition.idColumnDefinition().name(), id);
        var columns = tableDefinition.columns().values().stream().map(ColumnDefinition::name).collect(Collectors.joining(","));

        var result = session.selectOne(tableDefinition.tableName(), filterClause, columns);

        return entityParser.parse(entityType, result.columnLabels(), result.row(), tableDefinition);
    }

    public <T> List<T> findAll(Class<T> entityType) {
        var tableDefinition = entityContext.getEntityDefinition(entityType);
        var columns = tableDefinition.columns().values().stream().map(ColumnDefinition::name).collect(Collectors.joining(","));

        var result = session.selectAll(tableDefinition.tableName(), null, columns);

        return result.rows().stream()
                .map(row -> entityParser.parse(entityType, result.columnLabels(), row, tableDefinition))
                .toList();
    }

}
