package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.connection.sql.ParameterBinder;
import cz.tstrecha.foundry.orm.entity.type.TypeHandler;
import cz.tstrecha.foundry.orm.entity.type.TypeHandlerResolver;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

@ToString
public class ManagedColumn<E, F> {

    @Getter
    private final String columnLabel;
    private final Field field;
    private final TypeHandler<F> typeHandler;
    @Getter
    private final Class<F> fieldType;

    public ManagedColumn(String columnLabel, Field field) {
        this.columnLabel = columnLabel;
        this.field = field;
        this.field.setAccessible(true);

        this.fieldType = (Class<F>) field.getType();
        this.typeHandler = (TypeHandler<F>) TypeHandlerResolver.findHandlerForType(field.getType());
    }

    public ParameterBinder binderForEntity(E entity) {
        var value = getFieldValue(entity);
        return binderForValue(value);
    }

    public ParameterBinder binderForValue(F value) {
        return (statement, index) -> typeHandler.write(statement, index, value);
    }

    public ParameterBinder binderForAnyValue(Object value) {
        var typedValue = fieldType.cast(value);
        return (statement, index) -> typeHandler.write(statement, index, typedValue);
    }

    public F getFieldValue(E entity) {
        try {
            return (F) field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("This should never happen!", e);
        }
    }

    public void saturateColumnForEntity(E entity, ResultSet resultSet, int index) throws IllegalAccessException, SQLException {
        var parsedValue = typeHandler.read(resultSet, index);
        field.set(entity, parsedValue);
    }
}
