package cz.tstrecha.foundry.orm.entity;

import java.lang.reflect.Field;

public record ColumnDefinition(Field field, boolean isId, String name) {

}
