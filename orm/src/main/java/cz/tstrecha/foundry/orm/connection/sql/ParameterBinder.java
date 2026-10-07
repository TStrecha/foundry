package cz.tstrecha.foundry.orm.connection.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface ParameterBinder {

    void bind(PreparedStatement statement, int index) throws SQLException;

}
