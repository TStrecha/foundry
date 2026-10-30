package cz.tstrecha.foundry.orm.entity.descriptor.code;

import jakarta.persistence.EmbeddedId;

import java.lang.reflect.Field;
import java.util.List;

public record EmbeddedFieldDescriptor(
        Field field,
        Class<?> type,
        EmbeddableTypeDescriptor embeddableTypeDescriptor,

        boolean isId
) implements FieldDescriptor {

    public EmbeddedFieldDescriptor(Field field, EmbeddableTypeDescriptor embeddableTypeDescriptor) {
        var id = field.isAnnotationPresent(EmbeddedId.class);
        field.setAccessible(true);

        this(field, field.getType(), embeddableTypeDescriptor, id);
    }

    @Override
    public List<ColumnFieldDescriptor> flattenStructureToColumns() {
        return embeddableTypeDescriptor.flattenStructureToColumns();
    }
}
