package cz.tstrecha.foundry.orm.entity.descriptor.code;

import cz.tstrecha.foundry.orm.entity.descriptor.DescriptorRegistry;
import cz.tstrecha.foundry.orm.util.ReflectionUtils;
import jakarta.persistence.Entity;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record EntityDescriptor<E>(
        Class<E> type,
        String tableName,
        List<FieldDescriptor> fields,
        FieldDescriptor idDescriptor
) implements TypeDescriptor {

    public EntityDescriptor(Class<E> entityType, DescriptorRegistry registry) {
        var fields = ReflectionUtils.findAllFieldsInClass(entityType);

        var tableName = entityType.getAnnotation(Entity.class).name();

        var fieldDescriptors = fields.stream()
                .map(field -> FieldDescriptor.fromField(field, registry))
                .filter(Objects::nonNull)
                .toList();
        var idDescriptor = fieldDescriptors.stream()
                .filter(FieldDescriptor::isId)
                .findFirst()
                .orElseThrow();

        this(entityType, tableName, fieldDescriptors, idDescriptor);
    }

    @Override
    public List<ColumnFieldDescriptor> flattenStructureToColumns() {
        return fields.stream()
                .map(FieldDescriptor::flattenStructureToColumns)
                .flatMap(Collection::stream)
                .toList();
    }
}
