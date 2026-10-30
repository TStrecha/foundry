package cz.tstrecha.foundry.orm.operation.sql;

import cz.tstrecha.foundry.orm.operation.result.SelectOperationResult;
import lombok.Data;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Data
public class SequenceNextValueQuery implements ExecutableQuery<SelectOperationResult> {

    private final String sequenceName;

    @Override
    public String buildSql() {
        return "SELECT nextval('" + sequenceName + "')";
    }

    @Override
    public void bindParameters(PreparedStatement statement) {

    }

    @Override
    public SelectOperationResult executeStatement(PreparedStatement statement) throws SQLException {
        return new SelectOperationResult(statement, statement.executeQuery());
    }
}
