package cz.tstrecha.foundry.orm.entity.descriptor.code;

import jakarta.persistence.EmbeddedId;

import java.lang.reflect.Field;
import java.util.List;

public record EmbeddedFieldDescriptor(
        boolean isId,
        Field field,
        EmbeddableTypeDescriptor embeddableTypeDescriptor
) implements FieldDescriptor {

    public EmbeddedFieldDescriptor(Field field, EmbeddableTypeDescriptor embeddableTypeDescriptor) {
        var id = field.isAnnotationPresent(EmbeddedId.class);
        field.setAccessible(true);

        this(id, field, embeddableTypeDescriptor);
    }

    @Override
    public List<ColumnFieldDescriptor> flattenStructureToColumns() {
        return embeddableTypeDescriptor.flattenStructureToColumns();
    }

    @Override
    public Class<?> getFieldType() {
        return field.getType();
    }
}
