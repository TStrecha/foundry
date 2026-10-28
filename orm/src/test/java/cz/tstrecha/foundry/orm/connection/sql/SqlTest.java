package cz.tstrecha.foundry.orm.connection.sql;

import cz.tstrecha.foundry.orm.operation.sql.Sql;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SqlTest {

    @Test
    void selectReturnsSelectSql() {
        var sql = Sql.select("a", "b").from("t");
        assertThat(sql.buildSql()).isEqualTo("SELECT a,b FROM t");
    }

    @Test
    void selectAcceptsCollection() {
        var sql = Sql.select(List.of("x")).from("t");
        assertThat(sql.buildSql()).isEqualTo("SELECT x FROM t");
    }

    @Test
    void insertReturnsNewBuilder() {
        var insert = Sql.insert().into("t", List.of("a"))
                .values(List.of((stmt, idx) -> stmt.setString(idx, "v")));
        assertThat(insert.buildSql()).isEqualTo("INSERT INTO t(a) VALUES (?)");
    }

    @Test
    void updateBuildsSqlWithoutWhere() {
        var upd = Sql.update("t").set(Map.of("a", (stmt, idx) -> stmt.setLong(idx, 1L)));
        assertThat(upd.buildSql()).isEqualTo("UPDATE t SET a = ?");
    }

    @Test
    void selectWithoutColumnsIsRejected() {
        assertThatThrownBy(() -> Sql.select().from("t"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void selectWithNullColumnsVarargThrows() {
        assertThatThrownBy(() -> Sql.select((String[]) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void insertReturnsIndependentInstances() {
        var a = Sql.insert();
        var b = Sql.insert();
        assertThat(a).isNotSameAs(b);
    }

    @Test
    void updateWithNullTableIsRejected() {
        assertThatThrownBy(() -> Sql.update(null).set(Map.of("a", (stmt, i) -> stmt.setLong(i, 1L))).buildSql())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateWithBlankTableIsRejected() {
        assertThatThrownBy(() -> Sql.update("   ").set(Map.of("a", (stmt, i) -> stmt.setLong(i, 1L))).buildSql())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
