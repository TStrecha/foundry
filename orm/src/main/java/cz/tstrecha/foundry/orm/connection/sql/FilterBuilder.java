package cz.tstrecha.foundry.orm.connection.sql;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;
import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireValidSqlIdentifier;

@Getter
public class FilterBuilder {

    private final StringBuilder clause = new StringBuilder();
    private final List<ParameterBinder> parameters = new ArrayList<>();

    public FilterBuilder(String column, SqlOperator operator, ParameterBinder valueBinder) {
        clause.append(recordParameterAndFormatClause(column, operator, valueBinder));
    }

    public FilterBuilder and(String column, SqlOperator operator, ParameterBinder value) {
        clause.append(" AND ").append(recordParameterAndFormatClause(column, operator, value));
        return this;
    }

    public FilterBuilder or(String column, SqlOperator operator, ParameterBinder value) {
        clause.append(" OR ").append(recordParameterAndFormatClause(column, operator, value));
        return this;
    }

    private String recordParameterAndFormatClause(String column, SqlOperator operator, ParameterBinder value) {
        requireValidSqlIdentifier(column, "column");
        requireNotNullArgument(operator, "operator");
        requireNotNullArgument(value, "value");

        parameters.add(value);
        return String.format("\"%s\" %s ?", column, operator.getSqlOperator());
    }
}