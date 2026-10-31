package cz.tstrecha.foundry.orm.connection.sql;

import cz.tstrecha.foundry.orm.operation.result.BasicInsertOperationResult;
import cz.tstrecha.foundry.orm.operation.result.ReturningInsertOperationResult;
import cz.tstrecha.foundry.orm.operation.sql.InsertSql;
import cz.tstrecha.foundry.orm.operation.ParameterBinder;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void returningSingleColumnAppendsReturningClauseAfterValues() {
        var sql = new InsertSql()
                .into("book", List.of("title"))
                .values(List.of((stmt, i) -> stmt.setString(i, "x")))
                .returning(List.of("id"))
                .buildSql();

        assertThat(sql)
                .as("RETURNING must sit after VALUES — this is the hook the persist pipeline uses to pull db-generated values back onto the entity in a single round-trip")
                .isEqualTo("INSERT INTO book(title) VALUES (?) RETURNING id");
    }

    @Test
    void returningMultipleColumnsEmitsAllOfThemCommaSeparated() {
        var sql = new InsertSql()
                .into("event_log", List.of("event_type"))
                .values(List.of((stmt, i) -> stmt.setString(i, "x")))
                .returning(List.of("id", "occurrence_no", "audit_no"))
                .buildSql();

        assertThat(sql)
                .as("every post-insert generated column must be requested in one shot — splitting into multiple statements would need extra round-trips and lose transactional atomicity")
                .endsWith(" RETURNING id,occurrence_no,audit_no");
    }

    @Test
    void returningWithEmptyListProducesSqlWithoutReturningClause() {
        var sql = new InsertSql()
                .into("book", List.of("title"))
                .values(List.of((stmt, i) -> stmt.setString(i, "x")))
                .returning(List.of())
                .buildSql();

        assertThat(sql)
                .as("an INSERT whose caller declared zero post-insert columns must execute as a plain write — adding an empty RETURNING suffix would be a syntax error in Postgres")
                .doesNotContain("RETURNING");
    }

    @Test
    void buildWithoutReturningCallProducesSqlWithoutReturningClause() {
        var sql = new InsertSql()
                .into("book", List.of("title"))
                .values(List.of((stmt, i) -> stmt.setString(i, "x")))
                .buildSql();

        assertThat(sql)
                .as("RETURNING is strictly opt-in — a persister that has no post-insert columns must not emit it, otherwise the Basic flow is forced into a result-set path it does not need")
                .doesNotContain("RETURNING");
    }

    @Test
    void returningWithNullListIsRejected() {
        assertThatThrownBy(() -> new InsertSql().returning(null))
                .as("a null returning list is a caller bug — either you want to read columns back, or you do not; nulls here would silently fall through and depending on later code either NPE or omit RETURNING, which is the kind of ambiguity callers cannot debug")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void returningListContainingNullIsRejected() {
        assertThatThrownBy(() -> new InsertSql().returning(Arrays.asList("id", (String) null)))
                .as("a null column name inside the returning list would corrupt the SQL into ' RETURNING id,null' at best, or NPE at build time — must be caught at the setter so the error surfaces where the mistake lives")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void executeStatementWithReturningColumnsRunsExecuteQueryAndYieldsReturningResult() throws Exception {
        var statement = mock(PreparedStatement.class);
        var resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);

        var sql = new InsertSql()
                .into("book", List.of("title"))
                .values(List.of((stmt, i) -> stmt.setString(i, "x")))
                .returning(List.of("id"));

        var result = sql.executeStatement(statement);

        assertThat(result)
                .as("RETURNING requires executeQuery because the driver streams the generated rows as a ResultSet — executeUpdate would discard them, costing the caller the only hook they had to read generated ids")
                .isInstanceOf(ReturningInsertOperationResult.class);
        verify(statement).executeQuery();
    }

    @Test
    void executeStatementWithoutReturningColumnsRunsExecuteUpdateAndYieldsBasicResult() throws Exception {
        var statement = mock(PreparedStatement.class);
        when(statement.executeUpdate()).thenReturn(1);

        var sql = new InsertSql()
                .into("book", List.of("title"))
                .values(List.of((stmt, i) -> stmt.setString(i, "x")));

        var result = sql.executeStatement(statement);

        assertThat(result)
                .as("without RETURNING there is no result-set to consume — executeUpdate is the cheaper driver call, and the Basic variant carries just the row count the caller actually needs")
                .isInstanceOf(BasicInsertOperationResult.class);
        verify(statement).executeUpdate();
    }

    @Test
    void executeStatementWithEmptyReturningListFallsBackToExecuteUpdate() throws Exception {
        var statement = mock(PreparedStatement.class);
        when(statement.executeUpdate()).thenReturn(1);

        var sql = new InsertSql()
                .into("book", List.of("title"))
                .values(List.of((stmt, i) -> stmt.setString(i, "x")))
                .returning(List.of());

        var result = sql.executeStatement(statement);

        assertThat(result)
                .as("an explicit empty returning list must behave the same as never calling .returning() — otherwise callers writing dynamic builders would get different code paths for semantically identical requests")
                .isInstanceOf(BasicInsertOperationResult.class);
        verify(statement).executeUpdate();
    }
}
