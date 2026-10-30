package cz.tstrecha.foundry.orm.entity.type;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLType;
import java.util.UUID;

public class UUIDHandler implements TypeHandler<UUID> {

    @Override
    public UUID readValue(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getObject(index, UUID.class);
    }

    @Override
    public void writeValue(PreparedStatement statement, int index, UUID value) throws SQLException {
        statement.setObject(index, value);
    }

    @Override
    public void writeNull(PreparedStatement statement, int index) throws SQLException {
        statement.setObject(index, null);
    }

    @Override
    public SQLType getSqlType() {
        return JDBCType.JAVA_OBJECT;
    }
}
