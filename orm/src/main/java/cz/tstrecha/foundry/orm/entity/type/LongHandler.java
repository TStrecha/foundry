package cz.tstrecha.foundry.orm.entity.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LongHandler implements TypeHandler<Long> {

    @Override
    public Long read(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getLong(index);
    }

    @Override
    public void write(PreparedStatement statement, int index, Long value) throws SQLException {
        statement.setLong(index, value);
    }
}
