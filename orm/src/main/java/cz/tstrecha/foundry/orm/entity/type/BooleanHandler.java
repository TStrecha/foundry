package cz.tstrecha.foundry.orm.entity.type;

import java.sql.*;

public class BooleanHandler implements TypeHandler<Boolean> {

    @Override
    public Boolean readValue(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getBoolean(index);
    }

    @Override
    public void writeValue(PreparedStatement statement, int index, Boolean value) throws SQLException {
        statement.setBoolean(index, value);
    }

    @Override
    public SQLType getSqlType() {
        return JDBCType.BOOLEAN;
    }
}
