package cz.tstrecha.foundry.orm.connection.result;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.function.Function;

@RequiredArgsConstructor
public class SelectOperationResult implements DatabaseOperationResult {

    private final Statement statement;
    private final ResultSet resultSet;

    public <T> T mapToObject(Function<ResultSet, T> mappingFunction) {
        return mappingFunction.apply(resultSet);
    }

    @Override
    @SneakyThrows
    public void close() {
        resultSet.close();
        statement.close();
    }
}
