package cz.tstrecha.foundry.orm.entity.type;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UUIDHandler implements TypeHandler<UUID> {

    @Override
    public UUID readValue(ResultSet resultSet, int index) throws SQLException {
        var uuidString = resultSet.getString(index);
        if(uuidString == null) {
            return null;
        }

        return UUID.fromString(uuidString);
    }

    @Override
    public void writeValue(PreparedStatement statement, int index, UUID value) throws SQLException {
        statement.setString(index, value.toString());
    }

    @Override
    public void writeNull(PreparedStatement statement, int index) throws SQLException {
        statement.setNull(index, JDBCType.VARCHAR.getVendorTypeNumber());
    }
}
