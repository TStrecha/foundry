package cz.tstrecha.foundry.orm.connection.sql;

import java.util.LinkedList;

public record Query(String sql, LinkedList<Object> params) {
}
