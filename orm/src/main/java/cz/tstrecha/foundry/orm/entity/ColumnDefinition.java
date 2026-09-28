package cz.tstrecha.foundry.orm.entity;

import cz.tstrecha.foundry.orm.entity.type.ColumnType;

import java.lang.reflect.Field;

public record ColumnDefinition(Field field, boolean isId, String name, ColumnType type) {

}
