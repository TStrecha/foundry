package cz.tstrecha.foundry.orm.entity.descriptor.code;

import cz.tstrecha.foundry.orm.entity.descriptor.DescriptorRegistry;
import cz.tstrecha.foundry.orm.util.ReflectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record EmbeddableTypeDescriptor(
        Class<?> type,
        List<FieldDescriptor> fields
) implements TypeDescriptor {

    public EmbeddableTypeDescriptor(Class<?> type, DescriptorRegistry registry) {
        var fields = ReflectionUtils.findAllFieldsInClass(type).stream()
                .map(field -> FieldDescriptor.fromField(field, registry))
                .filter(Objects::nonNull)
                .toList();

        this(type, fields);
    }

    public List<ColumnFieldDescriptor> flattenStructureToColumns() {
        return fields.stream()
                .map(FieldDescriptor::flattenStructureToColumns)
                .flatMap(Collection::stream)
                .toList();
    }

}
