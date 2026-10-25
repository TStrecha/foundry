package cz.tstrecha.foundry.orm.entity.type;

import java.sql.*;

public class StringHandler implements TypeHandler<String> {

    @Override
    public String readValue(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getString(index);
    }

    @Override
    public void writeValue(PreparedStatement statement, int index, String value) throws SQLException {
        statement.setString(index, value);
    }

    @Override
    public void writeNull(PreparedStatement statement, int index) throws SQLException {
        statement.setString(index, null);
    }
}
