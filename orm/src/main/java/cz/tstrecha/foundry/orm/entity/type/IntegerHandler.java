package cz.tstrecha.foundry.orm.entity.type;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    public void writeNull(PreparedStatement statement, int index) throws SQLException {
        statement.setNull(index, JDBCType.INTEGER.getVendorTypeNumber());
    }
}
