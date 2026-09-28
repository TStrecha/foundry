package cz.tstrecha.foundry.orm.entity.parser.value;

import java.lang.reflect.Type;

public class ValueParserResolver {

    public static ValueParser<?> resolveForType(Type fieldType) {

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
