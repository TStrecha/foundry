package cz.tstrecha.foundry.orm.connection.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.function.Function;

@RequiredArgsConstructor
public class InsertOperationResult implements DatabaseOperationResult {

    private final Statement statement;

    @Getter
    private final int rowsCreated;

    @Override
    @SneakyThrows
    public void close() {
        statement.close();
    }
}
