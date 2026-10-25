package cz.tstrecha.foundry.orm.entity.type;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    public void writeNull(PreparedStatement statement, int index) throws SQLException {
        statement.setNull(index, JDBCType.BIGINT.getVendorTypeNumber());
    }
}
