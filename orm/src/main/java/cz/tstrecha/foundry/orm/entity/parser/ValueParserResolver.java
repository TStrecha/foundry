package cz.tstrecha.foundry.orm.entity.parser;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ValueParserResolver {

    private static final Map<Type, ValueParser<?>> parserLookupMap = new HashMap<>();

    public static ValueParser<?> findValueParserForType(Type fieldType) {
        return parserLookupMap.computeIfAbsent(fieldType, ValueParserResolver::resolveForType);
    }

    private static ValueParser<?> resolveForType(Type fieldType) {

        if(fieldType instanceof Class<?> clazz) {
            if(clazz == String.class) {
                return new StringValueParser();
            }
            if(clazz == Long.class) {
                return new LongValueParser();
            }
            if(clazz.isEnum()) {
                return new EnumValueParser<>((Class<Enum>) clazz);
            }
        }

        throw new IllegalArgumentException("Could not find a value parser for type " + fieldType.getTypeName());
    }
}
