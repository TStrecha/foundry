package cz.tstrecha.foundry.orm.entity.row;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {

    T map(ResultSet resultSet)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, SQLException;

}
