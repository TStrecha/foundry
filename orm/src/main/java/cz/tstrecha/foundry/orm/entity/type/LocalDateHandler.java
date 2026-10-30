package cz.tstrecha.foundry.orm.entity.type;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;

public class LocalDateHandler implements TypeHandler<LocalDate> {

    @Override
    public LocalDate readValue(ResultSet resultSet, int index) throws SQLException {
        var date = resultSet.getDate(index);

        if(date == null) {
            return null;
        }

        return date.toLocalDate();
    }

    @Override
    public void writeValue(PreparedStatement statement, int index, LocalDate value) throws SQLException {
        statement.setDate(index, Date.valueOf(value));
    }

    @Override
    public void writeNull(PreparedStatement statement, int index) throws SQLException {
        statement.setNull(index, JDBCType.DATE.getVendorTypeNumber());
    }
}
