package cz.tstrecha.foundry.orm.operation.filter;

import cz.tstrecha.foundry.orm.operation.ParameterBinder;
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

    public FilterBuilder(FilterClause filterClause) {
        this(filterClause.column(), filterClause.operator(), filterClause.valueBinder());
    }

    public FilterBuilder(List<FilterClause> clauses) {
        if (clauses.isEmpty()) {
            throw new IllegalArgumentException("clauses must not be empty");
        }

        var first = clauses.getFirst();
        clause.append(recordParameterAndFormatClause(
                first.column(), first.operator(), first.valueBinder()));

        for (int i = 1; i < clauses.size(); i++) {
            and(clauses.get(i));
        }
    }

    public FilterBuilder and(String column, SqlOperator operator, ParameterBinder value) {
        clause.append(" AND ").append(recordParameterAndFormatClause(column, operator, value));
        return this;
    }

    public FilterBuilder and(FilterClause filterClause) {
        return and(filterClause.column(), filterClause.operator(), filterClause.valueBinder());
    }

    public FilterBuilder or(String column, SqlOperator operator, ParameterBinder value) {
        clause.append(" OR ").append(recordParameterAndFormatClause(column, operator, value));
        return this;
    }

    public FilterBuilder or(FilterClause filterClause) {
        return or(filterClause.column(), filterClause.operator(), filterClause.valueBinder());
    }

    private String recordParameterAndFormatClause(String column, SqlOperator operator, ParameterBinder value) {
        requireValidSqlIdentifier(column, "column");
        requireNotNullArgument(operator, "operator");
        requireNotNullArgument(value, "value");

        parameters.add(value);
        return String.format("\"%s\" %s ?", column, operator.getSqlOperator());
    }
}