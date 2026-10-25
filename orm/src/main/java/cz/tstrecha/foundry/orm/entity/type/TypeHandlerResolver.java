package cz.tstrecha.foundry.orm.entity.type;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

public class TypeHandlerResolver {

    private static final Map<Type, TypeHandler<?>> handlerLookupMap = new HashMap<>();

    public static TypeHandler<?> findHandlerForType(Class<?> type) {
        requireNotNullArgument(type, "type");

        return handlerLookupMap.computeIfAbsent(type, TypeHandlerResolver::resolveForType);
    }

    private static TypeHandler<?> resolveForType(Type fieldType) {

        if(fieldType instanceof Class<?> clazz) {
            if(clazz == String.class) {
                return new StringHandler();
            }
            if(clazz == Long.class || clazz == long.class) {
                return new LongHandler();
            }
            if(clazz == Integer.class || clazz == int.class) {
                return new IntegerHandler();
            }
            if(clazz == Double.class || clazz == double.class) {
                return new DoubleHandler();
            }
            if(clazz == Boolean.class || clazz == boolean.class) {
                return new BooleanHandler();
            }
            if(clazz == UUID.class) {
                return new UUIDHandler();
            }
            if(clazz == byte[].class) {
                return new ByteArrayHandler();
            }
            if(clazz == BigDecimal.class) {
                return new BigDecimalHandler();
            }
            if(clazz.isEnum()) {
                return new EnumHandler<>((Class<Enum>) clazz);
            }
        }

        throw new IllegalArgumentException("Could not find a value parser for type " + fieldType.getTypeName());
    }
}
