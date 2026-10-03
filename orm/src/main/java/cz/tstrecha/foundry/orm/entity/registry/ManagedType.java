package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;
import cz.tstrecha.foundry.orm.definition.Table;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ManagedType<T> {

    @Getter
    private final Class<T> type;
    @Getter
    private final ManagedColumn<T> idColumn;
    @Getter
    private final String tableName;

    private final ManagedTypeCreator<T> entityCreator;
    private final Map<String, ManagedColumn<T>> columns;

    public ManagedType(Class<T> type) throws NoSuchMethodException {
        this.type = type;
        this.entityCreator = new ManagedTypeCreator<>(type);
        this.tableName = type.getAnnotation(Table.class).value();

        this.columns = new HashMap<>();
        ManagedColumn<T> idColumn = null;

        for (var field : type.getDeclaredFields()) {
            var columnDefinition = field.getAnnotation(Column.class);
            if(columnDefinition == null) {
                continue;
            }

            var managedColumn = new ManagedColumn<T>(columnDefinition.value(), field);
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

    public T createAndSaturateInstance(List<String> columnLabels, List<String> row) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        var instance = this.entityCreator.createNewEmptyInstance();

        for(int i = 0; i < columnLabels.size(); i++) {
            var label = columnLabels.get(i);
            var value = row.get(i);

            this.columns.get(label).saturateColumnForEntity(instance, value);
        }

        return instance;
    }

}
