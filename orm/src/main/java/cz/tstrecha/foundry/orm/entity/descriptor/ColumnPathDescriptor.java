package cz.tstrecha.foundry.orm.entity.descriptor;

import lombok.SneakyThrows;

import java.lang.reflect.Field;

public record ColumnPathDescriptor<T>(
        Field field,
        FieldPath fieldPath
) {

    @SneakyThrows
    public T readValueFromRoot(Object root) {
        var current = root;
        for(var node : fieldPath.nodes()) {
            current = node.get(current);
            if(current == null) {
                return null;
            }
        }
        return (T) this.field.get(current);
    }

    @SneakyThrows
    public void writeValueToRoot(Object root, Object value) {
        var current = root;
        for(var node : fieldPath.nodes()) {
            if(node.get(current) == null) {
                node.set(current, node.instantiate());
            }
            current = node.get(current);
        }
        this.field.set(current, value);
    }

}
