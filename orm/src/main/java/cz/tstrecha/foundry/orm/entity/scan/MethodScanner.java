package cz.tstrecha.foundry.orm.entity.scan;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

public class MethodScanner {

    public static <T> Method scanFieldSetter(Class<T> type, Field field) throws NoSuchMethodException {
        var setterMethodName = String.format("set%s", StringUtils.capitalize(field.getName()));

        return Arrays.stream(type.getMethods())
                .filter(method -> method.getName().equals(setterMethodName))
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0].equals(field.getType()))
                .findFirst()
                .orElseThrow(() -> new NoSuchMethodException("Couldn't find valid setter for field " + field.getName() + " in " + type.getName()));
    }

    public static <T> Constructor<T> scanNoArgsConstructor(Class<T> type) throws NoSuchMethodException {
        try {
            return type.getConstructor();
        } catch (NoSuchMethodException ex) {
            throw new NoSuchMethodException("Entities are required to have exactly one constructor with 0 arguments. No method found: " + ex.getMessage());
        }
    }

}
