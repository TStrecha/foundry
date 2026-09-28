package cz.tstrecha.foundry.orm.entity.scan;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.entity.EntityCreationStrategy;
import cz.tstrecha.foundry.orm.entity.EntityParameterInitializationStrategy;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityBuildingStrategyScanner {

    public static Map<Class<?>, EntityCreationStrategy<?>> scan(Set<Class<?>> entities) {
        return entities.stream().collect(Collectors.toMap(Function.identity(), EntityBuildingStrategyScanner::buildCreationStrategy));
    }

    @SneakyThrows
    private static <T> EntityCreationStrategy<T> buildCreationStrategy(Class<T> type) {
        var noArgConstructor = MethodScanner.scanNoArgsConstructor(type);

        var params = Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.getAnnotation(Column.class) != null)
                .toList();

        var parameterStrategies = params.stream()
                .collect(Collectors.toMap(Function.identity(), field -> buildColumnInitializationStrategy(field, type)));

        return new EntityCreationStrategy<>(type, noArgConstructor, parameterStrategies);
    }

    @SneakyThrows
    private static EntityParameterInitializationStrategy buildColumnInitializationStrategy(Field field, Class<?> entityType) {
        var setter = MethodScanner.scanFieldSetter(entityType, field);
        return new EntityParameterInitializationStrategy(field, setter);
    }
}
