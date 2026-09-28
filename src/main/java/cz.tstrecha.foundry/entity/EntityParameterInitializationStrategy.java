package cz.tstrecha.foundry.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public record EntityParameterInitializationStrategy(Field field, Method setter) {

}
