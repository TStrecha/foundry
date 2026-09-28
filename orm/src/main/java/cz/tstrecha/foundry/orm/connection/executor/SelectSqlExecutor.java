package cz.tstrecha.foundry.orm.connection.executor;

import cz.tstrecha.foundry.orm.connection.sql.Query;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.function.Function;

public class SelectSqlExecutor implements SqlExecutor {

    @SneakyThrows
    public <T> T executeQuery(Query query, Connection connection, Function<ResultSet, T> resultSetConverter) {
        System.out.println("Executing query: " + query.sql());
        var st = connection.prepareStatement(query.sql());
        var rs = st.executeQuery();

        var result = resultSetConverter.apply(rs);

        rs.close();
        st.close();

        return result;
    }
}
