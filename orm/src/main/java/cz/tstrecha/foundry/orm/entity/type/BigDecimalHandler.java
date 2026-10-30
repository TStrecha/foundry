package cz.tstrecha.foundry.orm.entity.type;

import java.math.BigDecimal;
import java.sql.*;

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
    public SQLType getSqlType() {
        return JDBCType.NUMERIC;
    }
}
