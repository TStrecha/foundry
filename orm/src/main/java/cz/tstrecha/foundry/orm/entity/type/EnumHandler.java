package cz.tstrecha.foundry.orm.entity.type;

import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor
public class EnumHandler<T extends Enum<T>> implements TypeHandler<Enum<T>> {

    private final Class<T> type;

    @Override
    public Enum<T> read(ResultSet resultSet, int index) throws SQLException {
        var value = resultSet.getString(index);
        return Enum.valueOf(type, value);
    }

    @Override
    public void write(PreparedStatement statement, int index, Enum<T> value) throws SQLException {
        statement.setString(index, value.name());
    }
}
