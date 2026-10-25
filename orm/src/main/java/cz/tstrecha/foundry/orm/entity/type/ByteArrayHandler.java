package cz.tstrecha.foundry.orm.entity.type;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ByteArrayHandler implements TypeHandler<byte[]> {

    @Override
    public byte[] readValue(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getBytes(index);
    }

    @Override
    public void writeValue(PreparedStatement statement, int index, byte[] value) throws SQLException {
        statement.setBytes(index, value);
    }

    @Override
    public void writeNull(PreparedStatement statement, int index) throws SQLException {
        statement.setNull(index, JDBCType.VARCHAR.getVendorTypeNumber());
    }
}
