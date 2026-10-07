package cz.tstrecha.foundry.orm.entity.type;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TypeHandlerResolver {

    private static final Map<Type, TypeHandler<?>> handlerLookupMap = new HashMap<>();

    public static TypeHandler<?> findHandlerForType(Class<?> type) {
        return handlerLookupMap.computeIfAbsent(type, TypeHandlerResolver::resolveForType);
    }

    private static TypeHandler<?> resolveForType(Type fieldType) {

        if(fieldType instanceof Class<?> clazz) {
            if(clazz == String.class) {
                return new StringHandler();
            }
            if(clazz == Long.class) {
                return new LongHandler();
            }
            if(clazz.isEnum()) {
                return new EnumHandler<>((Class<Enum>) clazz);
            }
        }

        throw new IllegalArgumentException("Could not find a value parser for type " + fieldType.getTypeName());
    }
}
