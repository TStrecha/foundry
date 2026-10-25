package cz.tstrecha.foundry.orm.entity.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface TypeHandler<T> {

    T readValue(ResultSet resultSet, int index) throws SQLException;
    void writeValue(PreparedStatement statement, int index, T value) throws SQLException;
    void writeNull(PreparedStatement statement, int index) throws SQLException;

    default T read(ResultSet resultSet, int index) throws SQLException {
        var value = readValue(resultSet, index);
        if(resultSet.wasNull()) {
            return null;
        }

        return value;
    }

    default void write(PreparedStatement statement, int index, T value) throws SQLException {
        if(value == null) {
            writeNull(statement, index);
        }
        else {
            writeValue(statement, index, value);
        }
    }

}
