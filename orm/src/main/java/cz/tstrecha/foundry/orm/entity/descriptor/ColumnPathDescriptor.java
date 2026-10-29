package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.entity.descriptor.code.ColumnFieldDescriptor;
import lombok.SneakyThrows;

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
        if(root == null) {
            return null;
        }

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
        if(rootValue) {
            throw new IllegalStateException("Cannot write to root");
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
