package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.entity.descriptor.code.ColumnFieldDescriptor;
import lombok.SneakyThrows;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

public record ColumnPathDescriptor<T>(
        Class<T> type,
        ColumnFieldDescriptor fieldDescriptor,
        FieldPath fieldPath,
        boolean rootValue
) {

    public ColumnPathDescriptor(ColumnFieldDescriptor fieldDescriptor, FieldPath fieldPath, boolean rootValue) {
        this((Class<T>) fieldDescriptor.getFieldType(), fieldDescriptor, fieldPath, rootValue);
    }

    @SneakyThrows
    public T readValueFromRoot(Object root) {
        requireNotNullArgument(root, "root");

        if(rootValue) {
            return (T) root;
        }

        var current = root;
        for(var node : fieldPath.nodes()) {
            current = node.get(current);
            if(current == null) {
                return null;
            }
        }
        return (T) this.fieldDescriptor.field().get(current);
    }

    @SneakyThrows
    public void writeValueToRoot(Object root, Object value) {
        requireNotNullArgument(root, "root");
        if(rootValue) {
            throw new UnsupportedOperationException("Cannot write to root");
        }

        var current = root;
        for(var node : fieldPath.nodes()) {
            if(node.get(current) == null) {
                node.set(current, node.instantiate());
            }
            current = node.get(current);
        }
        this.fieldDescriptor.field().set(current, value);
    }

}
