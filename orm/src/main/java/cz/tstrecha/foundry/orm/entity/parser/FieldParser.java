package cz.tstrecha.foundry.orm.entity.parser;

import cz.tstrecha.foundry.orm.entity.parser.value.ValueParserResolver;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class FieldParser<T> {

    private final Field field;
    private final Method fieldSetter;

    public void parse(T object, String fieldValue) throws IllegalAccessException, InvocationTargetException {
        var valueParser = ValueParserResolver.resolveForType(field.getType());
        fieldSetter.invoke(object, valueParser.fromString(fieldValue));
    }
}
