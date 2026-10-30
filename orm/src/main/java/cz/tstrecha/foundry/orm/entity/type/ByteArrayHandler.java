package cz.tstrecha.foundry.orm.entity.type;

import java.sql.*;

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
    public SQLType getSqlType() {
        return JDBCType.BINARY;
    }
}
