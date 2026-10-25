package cz.tstrecha.foundry.orm.connection.sql;

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
}
