package cz.tstrecha.foundry.orm.entity.persister;

import cz.tstrecha.foundry.orm.entity.descriptor.ColumnPathDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.ManagedColumnDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.PathFinder;
import cz.tstrecha.foundry.orm.entity.descriptor.code.EntityDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.code.FieldDescriptor;
import cz.tstrecha.foundry.orm.operation.ParameterBinder;
import cz.tstrecha.foundry.orm.entity.row.RowMapper;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

@Getter
public class ManagedType<T> {

    private final Class<T> type;
    private final String tableName;

    private final ManagedTypeCreator<T> entityCreator;
    private final Map<String, ManagedColumn<T, ?>> columns = new LinkedHashMap<>();
    private final List<ManagedColumn<T, ?>> idColumns = new ArrayList<>();

    private final FieldDescriptor idFieldDescriptor;

    public ManagedType(EntityDescriptor<T> entityDescriptor) throws NoSuchMethodException {
        requireNotNullArgument(entityDescriptor, "entityDescriptor");

        this.type = entityDescriptor.type();
        this.tableName = entityDescriptor.tableDescriptor().tableName();
        this.entityCreator = new ManagedTypeCreator<>(type);

        for(var columnPath : PathFinder.buildColumnPaths(entityDescriptor)) {
            var managedColumn = createManagedColumnFromPath(columnPath);
            columns.put(managedColumn.getColumnLabel(), managedColumn);
        }

        for(var columnPath : PathFinder.buildIdPaths(entityDescriptor)) {
            var managedColumn = createManagedColumnFromPath(columnPath);
            idColumns.add(managedColumn);
        }

        this.idFieldDescriptor = entityDescriptor.idDescriptor();
    }

    private <F> ManagedColumn<T, F> createManagedColumnFromPath(ManagedColumnDescriptor<F> managedColumnDescriptor) {
        return new ManagedColumn<>(managedColumnDescriptor);
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

    @SneakyThrows
    public Object extractKeyFromEntity(T entity) {
        requireNotNullArgument(entity, "entity");
        return idFieldDescriptor.field().get(entity);
    }

    //TODO: Better way
    public ManagedColumn<T, ?> getColumnByIndex(int i) {
        return new ArrayList<>(columns.values()).get(i);
    }

    public Set<String> getManagedColumnLabels() {
        return columns.keySet();
    }
}
