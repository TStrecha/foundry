package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.entity.descriptor.code.*;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

public class PathFinder {

    public static List<ColumnPathDescriptor<?>> buildColumnPaths(EntityDescriptor<?> entityDescriptor) {
        requireNotNullArgument(entityDescriptor, "entityDescriptor");
        return walkFields(new FieldPath(), entityDescriptor.fields());
    }

    public static List<ColumnPathDescriptor<?>> buildIdPaths(EntityDescriptor<?> entityDescriptor) {
        requireNotNullArgument(entityDescriptor, "entityDescriptor");

        if(entityDescriptor.idDescriptor() instanceof ColumnFieldDescriptor column) {
            return List.of(new ColumnPathDescriptor<>(column, new FieldPath(), true));
        }
        if(entityDescriptor.idDescriptor() instanceof EmbeddedFieldDescriptor embedded) {
            return walkFields(new FieldPath(), embedded.embeddableTypeDescriptor().fields());
        }

        throw new IllegalArgumentException("Unsupported entity descriptor type: " + entityDescriptor.idDescriptor().getClass().getName());
    }

    @SneakyThrows
    private static List<ColumnPathDescriptor<?>> walkFields(FieldPath path, List<FieldDescriptor> fields) {
        return fields.stream()
                .map(field -> walkField(path, field))
                .flatMap(Collection::stream).toList();
    }

    @SneakyThrows
    private static List<ColumnPathDescriptor<?>> walkField(FieldPath path, FieldDescriptor field) {
        if(field instanceof ColumnFieldDescriptor column) {
                return List.of(new ColumnPathDescriptor<>(column, path, false));
        } else if (field instanceof EmbeddedFieldDescriptor embedded) {
                var subPath = path.appendNew(embedded.field());
                return walkFields(subPath, embedded.embeddableTypeDescriptor().fields());
        }

        throw new IllegalArgumentException("Unsupported field type: " + field);
    }

}
