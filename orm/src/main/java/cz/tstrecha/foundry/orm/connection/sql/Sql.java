package cz.tstrecha.foundry.orm.connection.sql;

import java.util.Arrays;
import java.util.Collection;

public class Sql {

    public static SelectSql select(String... columns) {
        return new SelectSql(Arrays.stream(columns).toList());
    }

    public static SelectSql select(Collection<String> columns) {
        return new SelectSql(columns);
    }

}
