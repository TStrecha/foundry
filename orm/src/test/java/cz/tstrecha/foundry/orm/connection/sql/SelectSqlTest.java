package cz.tstrecha.foundry.orm.connection.sql;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.sql.PreparedStatement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class SelectSqlTest {

    @Test
    void buildsSelectWithoutWhere() {
        var sql = new SelectSql(List.of("a", "b")).from("t");
        assertThat(sql.buildSql()).isEqualTo("SELECT a,b FROM t");
    }

    @Test
    void buildsSelectWithWhere() {
        var filter = new FilterBuilder("a", SqlOperator.EQUALS, (stmt, i) -> stmt.setLong(i, 1L));
        var sql = new SelectSql(List.of("a")).from("t").where(filter);
        assertThat(sql.buildSql()).isEqualTo("SELECT a FROM t WHERE \"a\" = ?");
    }

    @Test
    void bindParametersIsNoOpWhenNoWhereClause() throws Exception {
        var sql = new SelectSql(List.of("a")).from("t");
        var stmt = mock(PreparedStatement.class);
        sql.bindParameters(stmt);
        verifyNoInteractions(stmt);
    }

    @Test
    void bindParametersIteratesFilterParametersInOrder() throws Exception {
        var filter = new FilterBuilder("a", SqlOperator.EQUALS, (stmt, i) -> stmt.setLong(i, 10L))
                .and("b", SqlOperator.EQUALS, (stmt, i) -> stmt.setLong(i, 20L));
        var sql = new SelectSql(List.of("a", "b")).from("t").where(filter);

        var stmt = mock(PreparedStatement.class);
        sql.bindParameters(stmt);
        InOrder inOrder = inOrder(stmt);
        inOrder.verify(stmt).setLong(1, 10L);
        inOrder.verify(stmt).setLong(2, 20L);
    }

    @Test
    void buildWithoutTableIsRejected() {
        var sql = new SelectSql(List.of("a"));
        assertThatThrownBy(sql::buildSql).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void buildWithNullColumnsIsRejected() {
        assertThatThrownBy(() -> new SelectSql(null).from("t"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fromWithNullTableIsRejected() {
        assertThatThrownBy(() -> new SelectSql(List.of("a")).from(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fromWithBlankTableIsRejected() {
        assertThatThrownBy(() -> new SelectSql(List.of("a")).from("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whereCalledTwiceReplacesPreviousFilter() {
        var first = new FilterBuilder("a", SqlOperator.EQUALS, (stmt, i) -> stmt.setLong(i, 1L));
        var second = new FilterBuilder("b", SqlOperator.EQUALS, (stmt, i) -> stmt.setLong(i, 2L));
        var sql = new SelectSql(List.of("a", "b")).from("t").where(first).where(second);
        assertThat(sql.buildSql()).isEqualTo("SELECT a,b FROM t WHERE \"b\" = ?");
    }

    @Test
    void fromCalledTwiceReplacesPreviousTable() {
        var sql = new SelectSql(List.of("a")).from("first").from("second");
        assertThat(sql.buildSql()).isEqualTo("SELECT a FROM second");
    }

    @Test
    void tableNameContainingSemicolonIsRejected() {
        assertThatThrownBy(() -> new SelectSql(List.of("a")).from("t; DROP TABLE users"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void columnLabelContainingCommaIsRejected() {
        assertThatThrownBy(() -> new SelectSql(List.of("a,b")).from("t").buildSql())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
