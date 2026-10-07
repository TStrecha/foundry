package cz.tstrecha.foundry.orm.connection.result;

import cz.tstrecha.foundry.orm.entity.row.RowMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class SelectOperationResult implements DatabaseOperationResult {

    private final Statement statement;
    @Getter
    private final ResultSet resultSet;

    public <T> Optional<T> mapToObject(RowMapper<T> rowMapper) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if(resultSet.next()) {
            var entity = rowMapper.map(resultSet);

            if(resultSet.next()) {
                throw new IllegalStateException("Query returned more then 1 row.");
            }

            return Optional.of(entity);
        } else {
            return Optional.empty();
        }
    }

    public <T> List<T> mapToObjects(RowMapper<T> rowMapper) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var mappedObjects = new ArrayList<T>();
        while (resultSet.next()) {
            mappedObjects.add(rowMapper.map(resultSet));
        }
        return mappedObjects;
    }

    @Override
    @SneakyThrows
    public void close() {
        resultSet.close();
        statement.close();
    }
}
