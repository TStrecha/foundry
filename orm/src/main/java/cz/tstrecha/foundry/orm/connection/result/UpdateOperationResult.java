package cz.tstrecha.foundry.orm.connection.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.Statement;

@RequiredArgsConstructor
public class UpdateOperationResult implements DatabaseOperationResult {

    private final Statement statement;

    @Getter
    private final int rowsCreated;

    @Override
    @SneakyThrows
    public void close() {
        statement.close();
    }
}
