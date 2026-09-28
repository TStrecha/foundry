package cz.tstrecha.foundry.orm.entity.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public record EntityParameterInitializationStrategy<T>(Field field, FieldParser<T> parser) {

}
