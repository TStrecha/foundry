package cz.tstrecha.foundry.orm.entity;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;
import cz.tstrecha.foundry.orm.definition.Table;
import cz.tstrecha.foundry.orm.entity.type.PostgresColumnTypeIdentifier;
import lombok.SneakyThrows;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityScanner {

    public static Map<Class<?>, EntityDefinition<?>> scan(Class<?>... scanningRoots) {
        //todo review
        var reflections = new Reflections(new ConfigurationBuilder().forPackages(Arrays.stream(scanningRoots).map(Class::getPackageName).distinct().toArray(String[]::new)));
        var entities = reflections.getTypesAnnotatedWith(Table.class);

        return entities.stream().collect(
                        HashMap::new,
                        (map, value) -> map.put(value, buildDefinition(value)),
                        HashMap::putAll);

    }

    public static Map<Class<?>, EntityCreationStrategy<?>> scanStrategies(Class<?>... scanningRoots) {
        //todo review
        var reflections = new Reflections(new ConfigurationBuilder().forPackages(Arrays.stream(scanningRoots).map(Class::getPackageName).distinct().toArray(String[]::new)));
        var entities = reflections.getTypesAnnotatedWith(Table.class);

        return entities.stream().collect(
                        HashMap::new,
                        (map, value) -> map.put(value, buildCreationStrategy(value)),
                        HashMap::putAll);

    }

    @SneakyThrows
    private static <T> EntityCreationStrategy<T> buildCreationStrategy(Class<T> type) {
        var noArgConstructor = type.getConstructor();

        var params = Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.getAnnotation(Column.class) != null)
                .toList();

        var parameterStrategies = params.stream()
                .collect(Collectors.toMap(Function.identity(), field -> buildColumnInitializationStrategy(field, type)));

        return new EntityCreationStrategy<>(type, noArgConstructor, parameterStrategies);
    }

    @SneakyThrows
    private static EntityParameterInitializationStrategy buildColumnInitializationStrategy(Field field, Class<?> entityType) {
        //TODO POC
        var cap = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        var setter = entityType.getMethod("set"+cap, field.getType());

        return new EntityParameterInitializationStrategy(field, setter);
    }

    private static EntityDefinition buildDefinition(Class<?> type) {
        var tableName = type.getAnnotation(Table.class).value();
        var columns = Arrays.stream(type.getDeclaredFields())
                .map(field -> new AbstractMap.SimpleEntry<>(field, field.getAnnotation(Column.class)))
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        var columnDefinitions = columns.entrySet().stream()
                .map(column -> buildColumnDefinition(column.getKey(), column.getValue()))
                .collect(Collectors.toMap(ColumnDefinition::name, Function.identity()));

        var idColumnDefinition = columnDefinitions.values().stream().filter(ColumnDefinition::isId).findFirst().orElse(null);

        return new EntityDefinition(type, tableName, idColumnDefinition, columnDefinitions);
    }

    @SneakyThrows
    private static ColumnDefinition buildColumnDefinition(Field field, Column definition) {
        var isId = field.getAnnotation(Id.class) != null;
        var type = PostgresColumnTypeIdentifier.identifyType(field.getType());

        return new ColumnDefinition(field, isId, definition.value(), type);
    }
}
