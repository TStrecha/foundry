package cz.tstrecha.foundry.orm.entity;

import cz.tstrecha.foundry.orm.connection.DBSession;
import cz.tstrecha.foundry.orm.connection.sql.Sql;
import cz.tstrecha.foundry.orm.entity.parser.EntityParser;
import lombok.RequiredArgsConstructor;

import java.util.List;

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

        var sql = Sql
                .select(tableDefinition.columns().values().stream().map(ColumnDefinition::name).toList())
                .from(tableDefinition.tableName())
                .where(filterClause);

        var result = session.selectOne(sql);

        return entityParser.parse(entityType, result.columnLabels(), result.row(), tableDefinition);
    }

    public <T> List<T> findAll(Class<T> entityType) {
        var tableDefinition = entityContext.getEntityDefinition(entityType);

        var sql = Sql
                .select(tableDefinition.columns().values().stream().map(ColumnDefinition::name).toList())
                .from(tableDefinition.tableName());

        var result = session.selectAll(sql);

        return result.rows().stream()
                .map(row -> entityParser.parse(entityType, result.columnLabels(), row, tableDefinition))
                .toList();
    }

}
