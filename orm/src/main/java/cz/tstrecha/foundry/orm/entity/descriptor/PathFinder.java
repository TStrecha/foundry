package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.entity.descriptor.code.*;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

public class PathFinder {

    public static List<ColumnPathDescriptor<?>> buildColumnPaths(EntityDescriptor entityDescriptor) {
        return walkFields(new FieldPath(), entityDescriptor.fields());
    }

    public static List<ColumnPathDescriptor<?>> buildIdPaths(EntityDescriptor entityDescriptor) {
        return walkField(false, new FieldPath(), entityDescriptor.idDescriptor());
    }

    @SneakyThrows
    private static List<ColumnPathDescriptor<?>> walkFields(FieldPath path, List<FieldDescriptor> fields) {
        var result = new ArrayList<ColumnPathDescriptor<?>>();

        for(var field : fields) {
            result.addAll(walkField(true, path, field));
        }

        return result;
    }

    @SneakyThrows
    private static List<ColumnPathDescriptor<?>> walkField(boolean appendField, FieldPath path, FieldDescriptor field) {
        if(field instanceof ColumnFieldDescriptor column) {
            return List.of(new ColumnPathDescriptor<>(column.field(), path));
        } else if (field instanceof EmbeddedFieldDescriptor embedded) {
            if(appendField) {
                var subPath = path.appendNew(embedded.field());
                return walkFields(subPath, embedded.embeddableTypeDescriptor().fields());
            } else {
                return walkFields(path, embedded.embeddableTypeDescriptor().fields());
            }
        }

        throw new IllegalArgumentException("Unsupported field type: " + field);
    }

}
