package cz.tstrecha.foundry.orm.entity.type;

import java.sql.*;

public class LongHandler implements TypeHandler<Long> {

    @Override
    public Long readValue(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getLong(index);
    }

    @Override
    public void writeValue(PreparedStatement statement, int index, Long value) throws SQLException {
        statement.setLong(index, value);
    }

    @Override
    public SQLType getSqlType() {
        return JDBCType.BIGINT;
    }
}
