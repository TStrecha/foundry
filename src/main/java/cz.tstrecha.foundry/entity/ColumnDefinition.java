package cz.tstrecha.foundry.entity;

import cz.tstrecha.foundry.entity.type.ColumnType;

import java.lang.reflect.Field;

public record ColumnDefinition(Field field, boolean isId, String name, ColumnType type) {

}
