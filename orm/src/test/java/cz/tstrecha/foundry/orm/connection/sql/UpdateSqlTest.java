package cz.tstrecha.foundry.orm.connection.sql;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.sql.PreparedStatement;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class UpdateSqlTest {

    @Test
    void buildsUpdateWithoutWhere() {
        var setters = new LinkedHashMap<String, ParameterBinder>();
        setters.put("title", (stmt, i) -> stmt.setString(i, "T"));
        var sql = new UpdateSql("book").set(setters);
        assertThat(sql.buildSql()).isEqualTo("UPDATE book SET title = ?");
    }

    @Test
    void buildsUpdateWithWhere() {
        var setters = new LinkedHashMap<String, ParameterBinder>();
        setters.put("title", (stmt, i) -> stmt.setString(i, "T"));
        var filter = new FilterBuilder("id", SqlOperator.EQUALS, (stmt, i) -> stmt.setLong(i, 1L));
        var sql = new UpdateSql("book").set(setters).where(filter);
        assertThat(sql.buildSql()).isEqualTo("UPDATE book SET title = ? WHERE \"id\" = ?");
    }

    @Test
    void bindParametersPlacesSetValuesThenFilterValues() throws Exception {
        var setters = new LinkedHashMap<String, ParameterBinder>();
        setters.put("title", (stmt, i) -> stmt.setString(i, "T"));
        setters.put("author", (stmt, i) -> stmt.setString(i, "A"));
        var filter = new FilterBuilder("id", SqlOperator.EQUALS, (stmt, i) -> stmt.setLong(i, 99L));
        var sql = new UpdateSql("book").set(setters).where(filter);

        var stmt = mock(PreparedStatement.class);
        sql.bindParameters(stmt);

        InOrder inOrder = inOrder(stmt);
        inOrder.verify(stmt).setString(1, "T");
        inOrder.verify(stmt).setString(2, "A");
        inOrder.verify(stmt).setLong(3, 99L);
    }

    @Test
    void bindParametersHandlesMultipleFilterParameters() throws Exception {
        var setters = new LinkedHashMap<String, ParameterBinder>();
        setters.put("title", (stmt, i) -> stmt.setString(i, "T"));
        var filter = new FilterBuilder("a", SqlOperator.EQUALS, (stmt, i) -> stmt.setLong(i, 1L))
                .and("b", SqlOperator.EQUALS, (stmt, i) -> stmt.setLong(i, 2L))
                .and("c", SqlOperator.EQUALS, (stmt, i) -> stmt.setLong(i, 3L));
        var sql = new UpdateSql("book").set(setters).where(filter);

        var stmt = mock(PreparedStatement.class);
        sql.bindParameters(stmt);

        InOrder inOrder = inOrder(stmt);
        inOrder.verify(stmt).setString(1, "T");
        inOrder.verify(stmt).setLong(2, 1L);
        inOrder.verify(stmt).setLong(3, 2L);
        inOrder.verify(stmt).setLong(4, 3L);
    }

    @Test
    void buildWithoutSetIsRejected() {
        var sql = new UpdateSql("book");
        assertThatThrownBy(sql::buildSql).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bindParametersWithoutSetIsRejected() {
        var sql = new UpdateSql("book");
        var stmt = mock(PreparedStatement.class);
        assertThatThrownBy(() -> sql.bindParameters(stmt))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void linkedHashMapPreservesColumnOrder() {
        var ordered = new LinkedHashMap<String, ParameterBinder>();
        ordered.put("a", (stmt, i) -> stmt.setLong(i, 1L));
        ordered.put("b", (stmt, i) -> stmt.setLong(i, 2L));
        assertThat(new UpdateSql("t").set(ordered).buildSql())
                .isEqualTo("UPDATE t SET a = ?,b = ?");
    }

    @Test
    void emptySetMapIsRejected() {
        assertThatThrownBy(() -> new UpdateSql("book").set(Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullTableIsRejected() {
        assertThatThrownBy(() -> new UpdateSql(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankTableIsRejected() {
        assertThatThrownBy(() -> new UpdateSql("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void setWithNullMapIsRejected() {
        assertThatThrownBy(() -> new UpdateSql("t").set(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void setCalledTwiceReplacesColumns() {
        var first = new LinkedHashMap<String, ParameterBinder>();
        first.put("a", (stmt, i) -> stmt.setLong(i, 1L));
        var second = new LinkedHashMap<String, ParameterBinder>();
        second.put("b", (stmt, i) -> stmt.setLong(i, 2L));
        var sql = new UpdateSql("t").set(first).set(second);
        assertThat(sql.buildSql()).isEqualTo("UPDATE t SET b = ?");
    }

    @Test
    void whereCalledTwiceReplacesFilter() {
        var setters = new LinkedHashMap<String, ParameterBinder>();
        setters.put("title", (stmt, i) -> stmt.setString(i, "T"));
        var first = new FilterBuilder("a", SqlOperator.EQUALS, (stmt, i) -> stmt.setLong(i, 1L));
        var second = new FilterBuilder("b", SqlOperator.EQUALS, (stmt, i) -> stmt.setLong(i, 2L));
        var sql = new UpdateSql("t").set(setters).where(first).where(second);
        assertThat(sql.buildSql()).isEqualTo("UPDATE t SET title = ? WHERE \"b\" = ?");
    }
}
