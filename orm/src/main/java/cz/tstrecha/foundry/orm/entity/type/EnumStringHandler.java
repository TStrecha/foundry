package cz.tstrecha.foundry.orm.entity.type;

import java.sql.*;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

public class EnumStringHandler<T extends Enum<T>> implements TypeHandler<Enum<T>> {

    private final Class<T> type;

    public EnumStringHandler(Class<T> type) {
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
    public SQLType getSqlType() {
        return JDBCType.VARCHAR;
    }
}
