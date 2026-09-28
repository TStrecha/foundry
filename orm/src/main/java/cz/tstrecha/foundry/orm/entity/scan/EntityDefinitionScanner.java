package cz.tstrecha.foundry.orm.entity.scan;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;
import cz.tstrecha.foundry.orm.definition.Table;
import cz.tstrecha.foundry.orm.entity.ColumnDefinition;
import cz.tstrecha.foundry.orm.entity.EntityDefinition;
import cz.tstrecha.foundry.orm.entity.type.PostgresColumnTypeIdentifier;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityDefinitionScanner {

    public static Map<Class<?>, EntityDefinition<?>> scan(Set<Class<?>> entities) {
        return entities.stream().collect(
                HashMap::new,
                (map, value) -> map.put(value, buildDefinition(value)),
                HashMap::putAll);

    }

    private static <T> EntityDefinition<T> buildDefinition(Class<T> type) {
        var tableName = type.getAnnotation(Table.class).value();
        var columnDefinitions = Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.getAnnotation(Column.class) != null)
                .map(column -> buildColumnDefinition(column, column.getAnnotation(Column.class)))
                .collect(Collectors.toMap(ColumnDefinition::name, Function.identity()));

        var idColumnDefinition = columnDefinitions.values().stream().filter(ColumnDefinition::isId).findFirst().orElse(null);

        return new EntityDefinition<T>(type, tableName, idColumnDefinition, columnDefinitions);
    }

    @SneakyThrows
    private static ColumnDefinition buildColumnDefinition(Field field, Column definition) {
        var isId = field.getAnnotation(Id.class) != null;
        var type = PostgresColumnTypeIdentifier.identifyType(field.getType());

        return new ColumnDefinition(field, isId, definition.value(), type);
    }
}
