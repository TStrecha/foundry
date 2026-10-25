package cz.tstrecha.foundry.orm.entity.type;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

public class EnumHandler<T extends Enum<T>> implements TypeHandler<Enum<T>> {

    private final Class<T> type;

    public EnumHandler(Class<T> type) {
        requireNotNullArgument(type, "type");

        this.type = type;
    }

    @Override
    public Enum<T> readValue(ResultSet resultSet, int index) throws SQLException {
        var value = resultSet.getString(index);
        if(value == null) {
            return null;
        }

        return Enum.valueOf(type, value);
    }

    @Override
    public void writeValue(PreparedStatement statement, int index, Enum<T> value) throws SQLException {
        statement.setString(index, value.name());
    }

    @Override
    public void writeNull(PreparedStatement statement, int index) throws SQLException {
        statement.setNull(index, JDBCType.VARCHAR.getVendorTypeNumber());
    }
}
