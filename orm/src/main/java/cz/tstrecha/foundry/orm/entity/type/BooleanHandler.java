package cz.tstrecha.foundry.orm.entity.type;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    public void writeNull(PreparedStatement statement, int index) throws SQLException {
        statement.setNull(index, JDBCType.BOOLEAN.getVendorTypeNumber());
    }
}
