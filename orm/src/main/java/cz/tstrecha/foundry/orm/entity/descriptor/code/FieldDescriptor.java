package cz.tstrecha.foundry.orm.entity.descriptor.code;

import cz.tstrecha.foundry.orm.entity.descriptor.DescriptorRegistry;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;

import java.lang.reflect.Field;
import java.util.List;

public sealed interface FieldDescriptor permits ColumnFieldDescriptor, EmbeddedFieldDescriptor {

    Class<?> getFieldType();
    boolean isId();
    List<ColumnFieldDescriptor> flattenStructureToColumns();
    Field field();

    static FieldDescriptor fromField(Field field, DescriptorRegistry registry) {
        if(field.isAnnotationPresent(Column.class)) {
            return new ColumnFieldDescriptor(field);
        }

        if(field.isAnnotationPresent(Embedded.class) || field.isAnnotationPresent(EmbeddedId.class)) {
            return new EmbeddedFieldDescriptor(field, (EmbeddableTypeDescriptor) registry.computeIfAbsent(field.getType()));
        }

        return null;
    }

}
