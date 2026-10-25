package cz.tstrecha.foundry.orm.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

@UtilityClass
public class ReflectionUtils {

    public static List<Field> findAllFieldsInClass(Class<?> type) {
        requireNotNullArgument(type, "type");

        var fields = new ArrayList<Field>();

        var currentType = type;

        while(currentType != null) {
            fields.addAll(Arrays.stream(currentType.getDeclaredFields()).toList());

            currentType = currentType.getSuperclass();
        }

        return fields;
    }
}
