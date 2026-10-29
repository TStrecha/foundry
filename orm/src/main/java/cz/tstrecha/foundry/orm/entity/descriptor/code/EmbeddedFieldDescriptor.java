package cz.tstrecha.foundry.orm.entity.descriptor.code;

import jakarta.persistence.EmbeddedId;

import java.lang.reflect.Field;

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
    public Class<?> getFieldType() {
        return field.getType();
    }
}
