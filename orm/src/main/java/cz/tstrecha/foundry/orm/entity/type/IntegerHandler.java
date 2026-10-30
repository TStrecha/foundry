package cz.tstrecha.foundry.orm.entity.type;

import java.sql.*;

public class IntegerHandler implements TypeHandler<Integer> {

    @Override
    public Integer readValue(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getInt(index);
    }

    @Override
    public void writeValue(PreparedStatement statement, int index, Integer value) throws SQLException {
        statement.setInt(index, value);
    }

    @Override
    public SQLType getSqlType() {
        return JDBCType.INTEGER;
    }
}
