package cz.tstrecha.foundry.orm.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

public record EntityCreationStrategy<T>(Class<T> entity, Constructor<T> noArgConstructor,
                                        Map<Field, EntityParameterInitializationStrategy> parameterStrategies) {
}
