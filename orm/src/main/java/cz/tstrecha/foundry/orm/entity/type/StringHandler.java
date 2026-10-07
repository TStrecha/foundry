package cz.tstrecha.foundry.orm.entity.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StringHandler implements TypeHandler<String> {

    @Override
    public String read(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getString(index);
    }

    @Override
    public void write(PreparedStatement statement, int index, String value) throws SQLException {
        statement.setString(index, value);
    }
}
