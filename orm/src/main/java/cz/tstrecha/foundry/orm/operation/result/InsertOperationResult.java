package cz.tstrecha.foundry.orm.operation.result;

import java.sql.SQLException;

public interface InsertOperationResult extends DatabaseOperationResult {

    int getRowsCreated();

    void fillEntity(ReturningValueConsumer consumer) throws SQLException;

}
