package cz.tstrecha.foundry.orm.operation.result;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ReturningValueConsumer {
    void consume(ResultSet resultSet) throws SQLException;
}