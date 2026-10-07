package cz.tstrecha.foundry.orm.entity.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface TypeHandler<T> {

    T read(ResultSet resultSet, int index) throws SQLException;
    void write(PreparedStatement statement, int index, T value) throws SQLException;

}
