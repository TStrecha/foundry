package cz.tstrecha.foundry.orm.connection.sql;

import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class FilterBuilderTest {

    @Test
    void initialConditionIsEmittedWithQuotedColumnAndOperator() {
        var fb = new FilterBuilder("name", SqlOperator.EQUALS, (stmt, i) -> stmt.setString(i, "x"));
        assertThat(fb.getClause().toString()).isEqualTo("\"name\" = ?");
        assertThat(fb.getParameters()).hasSize(1);
    }

    @Test
    void andAppendsSecondClauseInOrder() {
        var fb = new FilterBuilder("name", SqlOperator.EQUALS, (stmt, i) -> stmt.setString(i, "x"))
                .and("age", SqlOperator.GREATER, (stmt, i) -> stmt.setLong(i, 10L));
        assertThat(fb.getClause().toString()).isEqualTo("\"name\" = ? AND \"age\" > ?");
        assertThat(fb.getParameters()).hasSize(2);
    }

    @Test
    void orAppendsSecondClauseInOrder() {
        var fb = new FilterBuilder("name", SqlOperator.EQUALS, (stmt, i) -> stmt.setString(i, "x"))
                .or("alias", SqlOperator.EQUALS, (stmt, i) -> stmt.setString(i, "y"));
        assertThat(fb.getClause().toString()).isEqualTo("\"name\" = ? OR \"alias\" = ?");
    }

    @Test
    void mixingAndOrPreservesExactOrder() {
        var fb = new FilterBuilder("a", SqlOperator.EQUALS, (stmt, i) -> stmt.setLong(i, 1L))
                .and("b", SqlOperator.LESSER, (stmt, i) -> stmt.setLong(i, 2L))
                .or("c", SqlOperator.LIKE, (stmt, i) -> stmt.setString(i, "%z%"));
        assertThat(fb.getClause().toString())
                .isEqualTo("\"a\" = ? AND \"b\" < ? OR \"c\" LIKE ?");
        assertThat(fb.getParameters()).hasSize(3);
    }

    @Test
    void parametersCanBeInvokedToBindToStatement() throws Exception {
        var fb = new FilterBuilder("a", SqlOperator.EQUALS, (stmt, i) -> stmt.setString(i, "A"));
        var stmt = mock(PreparedStatement.class);
        fb.getParameters().get(0).bind(stmt, 7);
        org.mockito.Mockito.verify(stmt).setString(7, "A");
    }

    @Test
    void nullColumnIsRejected() {
        assertThatThrownBy(() ->
                new FilterBuilder(null, SqlOperator.EQUALS, (stmt, i) -> stmt.setLong(i, 1L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullOperatorThrows() {
        assertThatThrownBy(() -> new FilterBuilder("a", null, (stmt, i) -> stmt.setLong(i, 1L)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void columnLabelContainingQuoteIsEscapedOrRejected() {
        var fb = new FilterBuilder("a\" OR 1=1 --", SqlOperator.EQUALS,
                (stmt, i) -> stmt.setLong(i, 1L));
        assertThat(fb.getClause().toString())
                .as("column names in WHERE clauses must not permit injection")
                .doesNotContain("OR 1=1");
    }

    @Test
    void emptyColumnIsRejected() {
        assertThatThrownBy(() -> new FilterBuilder("", SqlOperator.EQUALS,
                (stmt, i) -> stmt.setLong(i, 1L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankColumnIsRejected() {
        assertThatThrownBy(() -> new FilterBuilder("   ", SqlOperator.EQUALS,
                (stmt, i) -> stmt.setLong(i, 1L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullBinderIsRejected() {
        assertThatThrownBy(() -> new FilterBuilder("a", SqlOperator.EQUALS, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chainOfManyAndConditionsIsBuiltCorrectly() {
        var fb = new FilterBuilder("c0", SqlOperator.EQUALS, (stmt, i) -> stmt.setLong(i, 0L));
        for (int i = 1; i < 10; i++) {
            int idx = i;
            fb.and("c" + idx, SqlOperator.EQUALS, (stmt, pos) -> stmt.setLong(pos, idx));
        }
        assertThat(fb.getParameters()).hasSize(10);
        assertThat(fb.getClause().toString())
                .startsWith("\"c0\" = ?")
                .endsWith("AND \"c9\" = ?");
    }
}
