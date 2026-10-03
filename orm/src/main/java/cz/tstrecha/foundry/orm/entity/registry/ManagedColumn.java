package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.entity.parser.ValueParser;
import cz.tstrecha.foundry.orm.entity.parser.ValueParserResolver;
import lombok.Getter;

import java.lang.reflect.Field;

public class ManagedColumn<T> {

    @Getter
    private final String columnLabel;
    private final Field field;
    private final ValueParser<?> valueParser;

    public ManagedColumn(String columnLabel, Field field) {
        this.columnLabel = columnLabel;
        this.field = field;
        this.field.setAccessible(true);

        this.valueParser = ValueParserResolver.findValueParserForType(field.getType());
    }

    public void saturateColumnForEntity(T entity, String value) throws IllegalAccessException {
        var parsedValue = valueParser.fromString(value);
        field.set(entity, parsedValue);
    }
}
