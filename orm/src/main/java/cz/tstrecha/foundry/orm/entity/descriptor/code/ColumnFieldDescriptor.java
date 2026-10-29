package cz.tstrecha.foundry.orm.entity.descriptor.code;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import java.lang.reflect.Field;
import java.util.List;

public record ColumnFieldDescriptor(
        boolean isId,
        String columnName,
        Field field
) implements FieldDescriptor {

    public ColumnFieldDescriptor(Field field) {
        var id = field.isAnnotationPresent(Id.class);
        var columnName = field.getAnnotation(Column.class).name();
        field.setAccessible(true);

        this(id, columnName, field);
    }

    @Override
    public Class<?> getFieldType() {
        return field.getType();
    }

}
