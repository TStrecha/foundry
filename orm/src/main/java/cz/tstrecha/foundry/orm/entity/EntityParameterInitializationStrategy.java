package cz.tstrecha.foundry.orm.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public record EntityParameterInitializationStrategy(Field field, Method setter) {

}
