package cz.tstrecha.foundry.orm.entity.type;

import java.sql.*;

public class DoubleHandler implements TypeHandler<Double> {

    @Override
    public Double readValue(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getDouble(index);
    }

    @Override
    public void writeValue(PreparedStatement statement, int index, Double value) throws SQLException {
        statement.setDouble(index, value);
    }

    @Override
    public SQLType getSqlType() {
        return JDBCType.DOUBLE;
    }
}
