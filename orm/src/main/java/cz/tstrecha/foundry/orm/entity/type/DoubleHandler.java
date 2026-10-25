package cz.tstrecha.foundry.orm.entity.type;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    public void writeNull(PreparedStatement statement, int index) throws SQLException {
        statement.setNull(index, JDBCType.DOUBLE.getVendorTypeNumber());
    }
}
