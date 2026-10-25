package cz.tstrecha.foundry.orm.util;

import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

@UtilityClass
public class ValidationUtils {

    private static final Pattern SQL_IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    public static void requireNotNullArgument(Object obj, String name) {
        requireNotNull(obj, name, ValidationType.ARGUMENT);
    }

    public static void requireNotEmptyArgument(Object obj, String name) {
        requireNotEmpty(obj, name, ValidationType.ARGUMENT);
    }

    public static void requireNotNullState(Object obj, String name) {
        requireNotNull(obj, name, ValidationType.STATE);
    }

    public static void requireNotEmptyState(Object obj, String name) {
        requireNotEmpty(obj, name, ValidationType.STATE);
    }

    public static void requireValidSqlIdentifier(String identifier, String name) {
        requireValidSqlIdentifier(identifier, name, ValidationType.ARGUMENT);
    }

    private static void requireNotNull(Object obj, String name, ValidationType type) {
        if(obj == null) {
            type.raiseException(name + " cannot be null");
        }
    }

    private static void requireNotEmpty(Object obj, String name, ValidationType type) {
        if(obj == null) {
            type.raiseException(name + " cannot be null");
        }

        if(obj instanceof Collection<?> collection && collection.isEmpty()) {
            type.raiseException(name + " cannot be empty");
        }
        if(obj instanceof Map<?, ?> map && map.isEmpty()) {
            type.raiseException(name + " cannot be empty");
        }
        if(obj instanceof String str && str.isBlank()) {
            type.raiseException(name + " cannot be empty");
        }
    }

    public static void requireValidSqlIdentifier(String identifier, String name, ValidationType type) {
        requireNotEmpty(identifier, name, type);
        if (!SQL_IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            type.raiseException(name + " must be a valid SQL identifier (letters, digits, underscore; must not start with a digit): " + identifier);
        }
    }

    @AllArgsConstructor
    public enum ValidationType {
        ARGUMENT(IllegalArgumentException::new),
        STATE(IllegalStateException::new);

        private Function<String, RuntimeException> exceptionProvider;

        public void raiseException(String message) {
            throw exceptionProvider.apply(message);
        }
    }
}
