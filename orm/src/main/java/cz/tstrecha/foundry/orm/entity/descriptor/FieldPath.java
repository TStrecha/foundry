package cz.tstrecha.foundry.orm.entity.descriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

public record FieldPath(
        List<PathNode<?>> nodes
) {

    public FieldPath() {
        this(new ArrayList<>());
    }

    public FieldPath appendNew(Field field) throws NoSuchMethodException {
        requireNotNullArgument(field, "field");

        var newPath = new FieldPath(new ArrayList<>(nodes));
        newPath.addNode(field);

        return newPath;
    }

    private void addNode(Field field) throws NoSuchMethodException {
        nodes.add(new PathNode<>(field));
    }

    public record PathNode<T>(Class<?> type,
                              Field field,
                              Constructor<T> creator) {

        public PathNode(Field field) throws NoSuchMethodException {
            var type = field.getType();
            var noArgsConstructor = type.getDeclaredConstructor();

            noArgsConstructor.setAccessible(true);
            field.setAccessible(true);

            this(type, field, (Constructor<T>) noArgsConstructor);
        }

        public Object get(Object obj) throws IllegalAccessException {
            return field.get(obj);
        }

        public void set(Object obj, Object value) throws IllegalAccessException {
            field.set(obj, value);
        }

        public T instantiate() throws InvocationTargetException, InstantiationException, IllegalAccessException {
            return creator.newInstance();
        }
    }
}
