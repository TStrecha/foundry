package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.connection.sql.ParameterBinder;
import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;
import cz.tstrecha.foundry.orm.definition.Table;
import cz.tstrecha.foundry.orm.entity.row.RowMapper;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ManagedType<T> {

    @Getter
    private final Class<T> type;
    @Getter
    private final ManagedColumn<T, ?> idColumn;
    @Getter
    private final String tableName;

    private final ManagedTypeCreator<T> entityCreator;
    private final Map<String, ManagedColumn<T, ?>> columns;

    public ManagedType(Class<T> type) throws NoSuchMethodException {
        this.type = type;
        this.entityCreator = new ManagedTypeCreator<>(type);
        this.tableName = type.getAnnotation(Table.class).value();

        this.columns = new HashMap<>();
        ManagedColumn<T, ?> idColumn = null;

        for (var field : type.getDeclaredFields()) {
            var columnDefinition = field.getAnnotation(Column.class);
            if(columnDefinition == null) {
                continue;
            }

            var managedColumn = new ManagedColumn<T, Object>(columnDefinition.value(), field);
            columns.put(columnDefinition.value(), managedColumn);

            if(field.getAnnotation(Id.class) != null) {
                idColumn = managedColumn;
            }
        }

        this.idColumn = idColumn;
    }

    public Set<String> getManagedColumnLabels() {
        return columns.keySet();
    }

    public RowMapper<T> createRowMapper(List<String> columnLabels) {
        var columnsOrdered = columnLabels.stream().map(columns::get).toList();

        return resultSet -> createAndSaturateInstance(columnsOrdered, resultSet);

    }

    private T createAndSaturateInstance(List<? extends ManagedColumn<T, ?>> columnsOrdered, ResultSet resultSet)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, SQLException {
        var instance = this.entityCreator.createNewEmptyInstance();

        for(int i = 0; i < columnsOrdered.size(); i++) {
            columnsOrdered.get(i).saturateColumnForEntity(instance, resultSet, i + 1);
        }

        return instance;
    }

    public List<ParameterBinder> getValueBindersForEntity(T entity) throws IllegalAccessException {
        var values = new ArrayList<ParameterBinder>();
        for (var managedColumn : columns.values()) {
            var fieldValue = managedColumn.binderForEntity(entity);
            values.add(fieldValue);
        }

        return values;
    }

}
