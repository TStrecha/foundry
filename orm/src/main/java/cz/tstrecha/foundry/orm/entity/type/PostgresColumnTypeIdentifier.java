package cz.tstrecha.foundry.orm.entity.type;

import java.lang.reflect.Type;

public class PostgresColumnTypeIdentifier {

    public static PostgresColumnType identifyType(Type type) {
        if(type == Long.class) {
            return PostgresColumnType.LONG;
        } else if (type == String.class) {
            return PostgresColumnType.VARCHAR;
        }

        return null;
    }
}
