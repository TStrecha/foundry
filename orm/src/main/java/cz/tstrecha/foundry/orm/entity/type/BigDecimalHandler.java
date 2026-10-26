package cz.tstrecha.foundry.orm.entity.type;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BigDecimalHandler implements TypeHandler<BigDecimal> {

    @Override
    public BigDecimal readValue(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getBigDecimal(index);
    }

    @Override
    public void writeValue(PreparedStatement statement, int index, BigDecimal value) throws SQLException {
        statement.setBigDecimal(index, value);
    }

    @Override
    public void writeNull(PreparedStatement statement, int index) throws SQLException {
        statement.setNull(index, JDBCType.NUMERIC.getVendorTypeNumber());
    }
}
