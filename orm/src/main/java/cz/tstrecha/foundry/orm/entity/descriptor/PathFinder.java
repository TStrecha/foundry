package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.entity.descriptor.code.*;
import cz.tstrecha.foundry.orm.entity.descriptor.db.SequenceDescriptor;
import lombok.SneakyThrows;

import java.util.Collection;
import java.util.List;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

public class PathFinder {

    public static List<ManagedColumnDescriptor<?>> buildColumnPaths(EntityDescriptor<?> entityDescriptor,
                                                                    DescriptorRegistry descriptorRegistry) {
        requireNotNullArgument(entityDescriptor, "entityDescriptor");
        return walkFields(entityDescriptor, new FieldPath(), entityDescriptor.fields(), descriptorRegistry);
    }

    public static List<ManagedColumnDescriptor<?>> buildIdPaths(EntityDescriptor<?> entityDescriptor,
                                                                DescriptorRegistry descriptorRegistry) {
        requireNotNullArgument(entityDescriptor, "entityDescriptor");

        if(entityDescriptor.idDescriptor() instanceof ColumnFieldDescriptor column) {
            var pathDescriptor = new ColumnPathDescriptor<>(column, new FieldPath(), true);
            var sequenceDescriptor = getSequenceDescriptorByGeneratorDescriptor(column.generatorDescriptor(), descriptorRegistry);

            return List.of(new ManagedColumnDescriptor<>(entityDescriptor, column, pathDescriptor, sequenceDescriptor));
        }
        if(entityDescriptor.idDescriptor() instanceof EmbeddedFieldDescriptor embedded) {
            return walkFields(entityDescriptor, new FieldPath(), embedded.embeddableTypeDescriptor().fields(), descriptorRegistry);
        }

        throw new IllegalArgumentException("Unsupported entity descriptor type: " + entityDescriptor.idDescriptor().getClass().getName());
    }

    @SneakyThrows
    private static List<ManagedColumnDescriptor<?>> walkFields(EntityDescriptor<?> entityDescriptor,
                                                               FieldPath path,
                                                               List<FieldDescriptor> fields,
                                                               DescriptorRegistry descriptorRegistry) {
        return fields.stream()
                .map(field -> walkField(entityDescriptor, path, field, descriptorRegistry))
                .flatMap(Collection::stream).toList();
    }

    @SneakyThrows
    private static List<ManagedColumnDescriptor<?>> walkField(EntityDescriptor<?> entityDescriptor,
                                                              FieldPath path,
                                                              FieldDescriptor field,
                                                              DescriptorRegistry descriptorRegistry) {
        if (field instanceof ColumnFieldDescriptor column) {
            var pathDescriptor = new ColumnPathDescriptor<>(column, path, false);
            var sequenceDescriptor = getSequenceDescriptorByGeneratorDescriptor(column.generatorDescriptor(), descriptorRegistry);

            return List.of(new ManagedColumnDescriptor<>(entityDescriptor, column, pathDescriptor, sequenceDescriptor));
        } else if (field instanceof EmbeddedFieldDescriptor embedded) {
            var subPath = path.appendNew(embedded.field());
            return walkFields(entityDescriptor, subPath, embedded.embeddableTypeDescriptor().fields(), descriptorRegistry);
        }

        throw new IllegalArgumentException("Unsupported field type: " + field);
    }

    private static SequenceDescriptor getSequenceDescriptorByGeneratorDescriptor(GeneratorDescriptor generatorDescriptor,
                                                                                 DescriptorRegistry descriptorRegistry) {
        if(generatorDescriptor == null || generatorDescriptor.name() == null) {
            return null;
        }

        var sequenceDescriptor = descriptorRegistry.getSequences().get(generatorDescriptor.name());
        if(sequenceDescriptor == null) {
            throw new IllegalStateException("No sequence descriptor found for generator: " + generatorDescriptor.name());
        }

        return sequenceDescriptor;
    }

}
