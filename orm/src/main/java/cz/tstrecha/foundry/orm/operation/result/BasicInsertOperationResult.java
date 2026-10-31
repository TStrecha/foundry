package cz.tstrecha.foundry.orm.operation.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.Statement;

@RequiredArgsConstructor
public class BasicInsertOperationResult implements InsertOperationResult {

    private final Statement statement;

    @Getter
    private final int rowsCreated;

    @Override
    @SneakyThrows
    public void close() {
        statement.close();
    }

    @Override
    public void fillEntity(ReturningValueConsumer consumer) {

    }
}
