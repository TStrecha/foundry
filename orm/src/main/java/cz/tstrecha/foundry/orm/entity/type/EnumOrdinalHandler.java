package cz.tstrecha.foundry.orm.entity.type;

import java.sql.*;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

public class EnumOrdinalHandler<T extends Enum<T>> implements TypeHandler<Enum<T>> {

    private final Class<T> type;

    public EnumOrdinalHandler(Class<T> type) {
        requireNotNullArgument(type, "type");
        this.type = type;
    }

    @Override
    public Enum<T> readValue(ResultSet resultSet, int index) throws SQLException {
        var value = resultSet.getInt(index);
        if(resultSet.wasNull()) {
            return null;
        }

        return type.getEnumConstants()[value];
    }

    @Override
    public void writeValue(PreparedStatement statement, int index, Enum<T> value) throws SQLException {
        statement.setInt(index, value.ordinal());
    }

    @Override
    public SQLType getSqlType() {
        return JDBCType.INTEGER;
    }
}
