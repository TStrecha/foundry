package cz.tstrecha.foundry.orm.entity.descriptor.code;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public record ColumnFieldDescriptor(
        Field field,
        Class<?> type,

        boolean isId,
        String columnName,
        EnumType enumType
) implements FieldDescriptor {

    public ColumnFieldDescriptor(Field field) {
        var type = field.getType();
        var id = field.isAnnotationPresent(Id.class);
        var columnName = field.getAnnotation(Column.class).name();
        field.setAccessible(true);

        EnumType enumType = null;

        if(type.isEnum()) {
            enumType = Optional.ofNullable(field.getAnnotation(Enumerated.class))
                    .map(Enumerated::value)
                    .orElse(EnumType.ORDINAL);
        }

        this(field, type, id, columnName, enumType);
    }

    @Override
    public List<ColumnFieldDescriptor> flattenStructureToColumns() {
        return List.of(this);
    }

}
