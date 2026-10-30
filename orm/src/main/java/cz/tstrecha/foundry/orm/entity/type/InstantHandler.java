package cz.tstrecha.foundry.orm.entity.type;

import java.sql.*;
import java.time.Instant;

public class InstantHandler implements TypeHandler<Instant> {

    @Override
    public Instant readValue(ResultSet resultSet, int index) throws SQLException {
        var timestamp = resultSet.getTimestamp(index);

        if(timestamp == null) {
            return null;
        }

        return timestamp.toInstant();
    }

    @Override
    public void writeValue(PreparedStatement statement, int index, Instant value) throws SQLException {
        var timestamp = new Timestamp(value.toEpochMilli());
        timestamp.setNanos(value.getNano());

        statement.setTimestamp(index, timestamp);
    }

    @Override
    public SQLType getSqlType() {
        return JDBCType.TIMESTAMP;
    }
}
