package cz.tstrecha.foundry.orm.operation.filter;

import cz.tstrecha.foundry.orm.operation.ParameterBinder;

public record FilterClause(String column, SqlOperator operator, ParameterBinder valueBinder) {


}
