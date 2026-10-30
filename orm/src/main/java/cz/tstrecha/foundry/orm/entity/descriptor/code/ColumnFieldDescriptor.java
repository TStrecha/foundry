package cz.tstrecha.foundry.orm.entity.descriptor.code;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import java.lang.reflect.Field;

public record ColumnFieldDescriptor(
        Field field,
        Class<?> type,

        boolean isId,
        String columnName
) implements FieldDescriptor {

    public ColumnFieldDescriptor(Field field) {
        var id = field.isAnnotationPresent(Id.class);
        var columnName = field.getAnnotation(Column.class).name();
        field.setAccessible(true);

        this(field, field.getType(), id, columnName);
    }

}
