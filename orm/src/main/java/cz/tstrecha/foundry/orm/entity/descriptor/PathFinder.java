package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.entity.descriptor.code.*;
import lombok.SneakyThrows;

import java.util.Collection;
import java.util.List;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

public class PathFinder {

    public static List<ManagedColumnDescriptor<?>> buildColumnPaths(EntityDescriptor<?> entityDescriptor) {
        requireNotNullArgument(entityDescriptor, "entityDescriptor");
        return walkFields(entityDescriptor, new FieldPath(), entityDescriptor.fields());
    }

    public static List<ManagedColumnDescriptor<?>> buildIdPaths(EntityDescriptor<?> entityDescriptor) {
        requireNotNullArgument(entityDescriptor, "entityDescriptor");

        if(entityDescriptor.idDescriptor() instanceof ColumnFieldDescriptor column) {
            var pathDescriptor = new ColumnPathDescriptor<>(column, new FieldPath(), true);

            return List.of(new ManagedColumnDescriptor<>(entityDescriptor, column, pathDescriptor));
        }
        if(entityDescriptor.idDescriptor() instanceof EmbeddedFieldDescriptor embedded) {
            return walkFields(entityDescriptor, new FieldPath(), embedded.embeddableTypeDescriptor().fields());
        }

        throw new IllegalArgumentException("Unsupported entity descriptor type: " + entityDescriptor.idDescriptor().getClass().getName());
    }

    @SneakyThrows
    private static List<ManagedColumnDescriptor<?>> walkFields(EntityDescriptor<?> entityDescriptor,
                                                               FieldPath path,
                                                               List<FieldDescriptor> fields) {
        return fields.stream()
                .map(field -> walkField(entityDescriptor, path, field))
                .flatMap(Collection::stream).toList();
    }

    @SneakyThrows
    private static List<ManagedColumnDescriptor<?>> walkField(EntityDescriptor<?> entityDescriptor,
                                                              FieldPath path,
                                                              FieldDescriptor field) {
        if (field instanceof ColumnFieldDescriptor column) {
            var pathDescriptor = new ColumnPathDescriptor<>(column, path, false);

            return List.of(new ManagedColumnDescriptor<>(entityDescriptor, column, pathDescriptor));
        } else if (field instanceof EmbeddedFieldDescriptor embedded) {
            var subPath = path.appendNew(embedded.field());
            return walkFields(entityDescriptor, subPath, embedded.embeddableTypeDescriptor().fields());
        }

        throw new IllegalArgumentException("Unsupported field type: " + field);
    }

}
