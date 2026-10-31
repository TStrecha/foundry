package cz.tstrecha.foundry.orm.entity.persister;

import cz.tstrecha.foundry.orm.entity.descriptor.ColumnPathDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.ManagedColumnDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.code.ColumnFieldDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.db.ColumnDescriptor;
import cz.tstrecha.foundry.orm.entity.persister.generator.PostInsertValueGenerator;
import cz.tstrecha.foundry.orm.entity.persister.generator.ValueGenerator;
import cz.tstrecha.foundry.orm.operation.ParameterBinder;
import cz.tstrecha.foundry.orm.entity.type.TypeHandler;
import cz.tstrecha.foundry.orm.entity.type.TypeHandlerResolver;
import lombok.Getter;
import lombok.ToString;

import java.sql.ResultSet;
import java.sql.SQLException;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

@Getter
@ToString
public class ManagedColumn<E, F> {

    private final String columnLabel;
    private final Class<F> fieldType;
    private final ColumnDescriptor columnDescriptor;
    private final ColumnFieldDescriptor fieldDescriptor;
    private final ColumnPathDescriptor<F> columnPathDescriptor;
    private final TypeHandler<F> typeHandler;

    private final ValueGenerator<?> valueGenerator;

    public ManagedColumn(ManagedColumnDescriptor<F> managedColumnDescriptor) {
        requireNotNullArgument(managedColumnDescriptor, "managedColumnDescriptor");

        this.columnDescriptor = managedColumnDescriptor.column();
        this.fieldDescriptor = managedColumnDescriptor.field();
        this.columnPathDescriptor = managedColumnDescriptor.entityPath();
        this.columnLabel = fieldDescriptor.columnName();
        this.fieldType = columnPathDescriptor.type();

        this.valueGenerator = ValueGenerator.fromGeneratorDescriptor(
                managedColumnDescriptor.field().generatorDescriptor(), managedColumnDescriptor.sequenceDescriptor());

        this.typeHandler = (TypeHandler<F>) TypeHandlerResolver.findHandlerForType(fieldDescriptor.type(), fieldDescriptor.enumType());
    }

    public ParameterBinder binderForEntity(E entity) {
        var value = getFieldValue(entity);
        return binderForValue(value);
    }

    public ParameterBinder binderForValue(F value) {
        return (statement, index) -> typeHandler.write(statement, index, value);
    }

    public ParameterBinder binderForAnyValue(Object value) {
        if(value == null) {
            return (statement, index) -> typeHandler.write(statement, index, null);
        }

        if(value.getClass() != fieldType){
            throw new ClassCastException("Value of type " + value.getClass().getName() + " is not of type " + fieldType.getName());
        }

        var typedValue = fieldType.cast(value);
        return (statement, index) -> typeHandler.write(statement, index, typedValue);
    }

    public ParameterBinder binderForRoot(Object value) {
        var leafValue = columnPathDescriptor.readValueFromRoot(value);
        return binderForValue(leafValue);
    }

    public F getFieldValue(E entity) {
        requireNotNullArgument(entity, "entity");
        return columnPathDescriptor.readValueFromRoot(entity);
    }

    public void setFieldValue(E entity, Object value) {
        requireNotNullArgument(entity, "entity");
        columnPathDescriptor.writeValueToRoot(entity, value);
    }

    public void saturateColumnForEntity(E entity, ResultSet resultSet, int index) throws SQLException {
        requireNotNullArgument(resultSet, "resultSet");
        var parsedValue = typeHandler.read(resultSet, index);
        columnPathDescriptor.writeValueToRoot(entity, parsedValue);
    }

    public boolean canGenerateValue() {
        return valueGenerator != null;
    }

    public boolean isId() {
        return fieldDescriptor.isId();
    }

    public void readAndConsumePostGeneratedValue(E entity, ResultSet rs, int i) throws SQLException {
        var postInsertGenerator = (PostInsertValueGenerator<?>) getValueGenerator();
        var value = postInsertGenerator.createValueConsumer().consume(rs, i);
        setFieldValue(entity, value);
    }
}
