package cz.tstrecha.foundry.orm.entity.persister;

import cz.tstrecha.foundry.orm.operation.ParameterBinder;
import cz.tstrecha.foundry.orm.entity.row.RowMapper;
import cz.tstrecha.foundry.orm.util.ReflectionUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

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
        requireNotNullArgument(type, "type");

        this.type = type;
        this.entityCreator = new ManagedTypeCreator<>(type);
        this.tableName = Optional.ofNullable(type.getAnnotation(Entity.class))
                .map(Entity::name)
                .orElseThrow(() -> new IllegalArgumentException("Class " + type.getName() + " cannot be managed, as it is not annotated with @Table"));

        this.columns = new LinkedHashMap<>();
        ManagedColumn<T, ?> idColumn = null;

        for (var field : ReflectionUtils.findAllFieldsInClass(type)) {
            var columnDefinition = field.getAnnotation(Column.class);
            if(columnDefinition == null) {
                continue;
            }

            var managedColumn = new ManagedColumn<T, Object>(columnDefinition.name(), field);
            columns.put(columnDefinition.name(), managedColumn);

            if(field.getAnnotation(Id.class) != null) {
                if(idColumn != null) {
                    throw new IllegalStateException("Multiple ids found for " + type.getName());
                }

                idColumn = managedColumn;
            }
        }

        if(idColumn == null) {
            throw new IllegalStateException("No id column found for " + type.getName());
        }

        this.idColumn = idColumn;
    }

    public RowMapper<T> createRowMapper(List<String> columnLabels) {
        var columnsOrdered = columnLabels.stream()
                .map(label -> {
                    var column = columns.get(label);
                    if(column == null) {
                        throw new IllegalArgumentException("Cannot map label " + label + " to any managed column for type " + type.getName());
                    }

                    return column;
                })
                .toList();

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

    public List<ParameterBinder> getValueBindersForEntity(T entity) {
        return getValueBindersForEntity(entity, columns.values());
    }

    public List<ParameterBinder> getValueBindersForEntity(T entity, Collection<ManagedColumn<T, ?>> columns) {
        var values = new ArrayList<ParameterBinder>();
        for (var managedColumn : columns) {
            var fieldValue = managedColumn.binderForEntity(entity);
            values.add(fieldValue);
        }

        return values;
    }

    public List<?> createEntitySnapshot(T entity) {
        return columns.values().stream().map(e -> e.getFieldValue(entity)).toList();
    }

    public Object extractKeyFromEntity(T entity) {
        return getIdColumn().getFieldValue(entity);
    }

    //TODO: Better way
    public ManagedColumn<T, ?> getColumnByIndex(int i) {
        return new ArrayList<>(columns.values()).get(i);
    }

    public Set<String> getManagedColumnLabels() {
        return columns.keySet();
    }
}
