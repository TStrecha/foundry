package cz.tstrecha.foundry.orm.connection.sql;

import cz.tstrecha.foundry.orm.operation.sql.InsertSql;
import cz.tstrecha.foundry.orm.operation.ParameterBinder;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class InsertSqlTest {

    @Test
    void buildsSingleRowInsert() {
        var sql = new InsertSql()
                .into("book", List.of("id", "title"))
                .values(List.of(
                        (stmt, i) -> stmt.setLong(i, 1L),
                        (stmt, i) -> stmt.setString(i, "a")));
        assertThat(sql.buildSql()).isEqualTo("INSERT INTO book(id,title) VALUES (?,?)");
    }

    @Test
    void bindParametersCallsBindersInOrder() throws Exception {
        var sql = new InsertSql()
                .into("book", List.of("id", "title"))
                .values(List.of(
                        (stmt, i) -> stmt.setLong(i, 1L),
                        (stmt, i) -> stmt.setString(i, "a")));

        var stmt = mock(PreparedStatement.class);
        sql.bindParameters(stmt);

        InOrder inOrder = inOrder(stmt);
        inOrder.verify(stmt).setLong(1, 1L);
        inOrder.verify(stmt).setString(2, "a");
    }

    @Test
    void buildWithoutIntoCallIsRejected() {
        var sql = new InsertSql().values(List.of((stmt, i) -> stmt.setLong(i, 1L)));
        assertThatThrownBy(sql::buildSql).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void buildWithoutValuesCallIsRejected() {
        var sql = new InsertSql().into("book", List.of("id"));
        assertThatThrownBy(sql::buildSql).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mismatchedColumnAndValueCountsAreRejected() {
        var sql = new InsertSql()
                .into("book", List.of("id", "title", "author"))
                .values(List.of((stmt, i) -> stmt.setLong(i, 1L)));
        assertThatThrownBy(sql::buildSql).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void emptyColumnsAndValuesAreRejected() {
        var sql = new InsertSql().into("book", List.of()).values(List.of());
        assertThatThrownBy(sql::buildSql).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void intoWithNullTableIsRejected() {
        assertThatThrownBy(() -> new InsertSql().into(null, List.of("a")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void intoWithNullColumnsIsRejected() {
        assertThatThrownBy(() -> new InsertSql().into("t", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void valuesWithNullIsRejected() {
        assertThatThrownBy(() -> new InsertSql().into("t", List.of("a")).values(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void valuesWithNullBinderInListIsRejected() {
        assertThatThrownBy(() -> new InsertSql()
                .into("t", List.of("a"))
                .values(Arrays.asList((ParameterBinder) null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tableNameContainingSemicolonIsRejected() {
        assertThatThrownBy(() -> new InsertSql().into("t; DROP TABLE users", List.of("a")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tableNameContainingDoubleQuoteIsRejected() {
        assertThatThrownBy(() -> new InsertSql().into("t\" OR 1=1 --", List.of("a")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tableNameStartingWithDigitIsRejected() {
        assertThatThrownBy(() -> new InsertSql().into("1table", List.of("a")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void columnNameContainingDoubleQuoteIsRejected() {
        assertThatThrownBy(() -> new InsertSql().into("t", List.of("a\" OR 1=1"))
                .values(List.of((stmt, i) -> stmt.setLong(i, 1L))).buildSql())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void columnNameContainingSemicolonIsRejected() {
        assertThatThrownBy(() -> new InsertSql().into("t", List.of("a; DROP"))
                .values(List.of((stmt, i) -> stmt.setLong(i, 1L))).buildSql())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
