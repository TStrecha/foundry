package cz.tstrecha.foundry.orm.operation.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

@RequiredArgsConstructor
public class ReturningInsertOperationResult implements InsertOperationResult {

    private final Statement statement;
    private final ResultSet resultSet;

    @Getter
    private int rowsCreated;

    @Override
    @SneakyThrows
    public void close() {
        statement.close();
        resultSet.close();
    }

    public void fillEntity(ReturningValueConsumer consumer) throws SQLException {
        requireNotNullArgument(consumer, "consumer");

        while(resultSet.next()) {
            rowsCreated++;
            consumer.consume(resultSet);
        }
    }
}
