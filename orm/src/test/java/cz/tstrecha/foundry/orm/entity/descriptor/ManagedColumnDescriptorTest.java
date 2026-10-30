package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.fixtures.TestDescriptors;
import cz.tstrecha.foundry.orm.fixtures.embedded.CompositeKeyFixtureEntity;
import cz.tstrecha.foundry.orm.fixtures.embedded.UserFixtureEntity;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import org.junit.jupiter.api.Test;

import java.sql.JDBCType;

import static org.assertj.core.api.Assertions.assertThat;

class ManagedColumnDescriptorTest {

    private ManagedColumnDescriptor<?> columnPathFor(Class<?> entity, String columnName) {
        return TestDescriptors.columnPathsFor(entity).stream()
                .filter(p -> columnName.equals(p.field().columnName()))
                .findFirst().orElseThrow();
    }

    private ManagedColumnDescriptor<?> idPathFor(Class<?> entity, String columnName) {
        return TestDescriptors.idPathsFor(entity).stream()
                .filter(p -> columnName.equals(p.field().columnName()))
                .findFirst().orElseThrow();
    }

    @Test
    void topLevelColumnExposesDbColumnDescriptor() {
        var mcd = columnPathFor(BookEntity.class, "title");

        assertThat(mcd.column())
                .as("column() must carry the db-side shape — callers rendering SQL cannot be expected to reach back into reflection to rebuild it")
                .isNotNull();
    }

    @Test
    void dbColumnDescriptorNameMatchesCodeColumnName() {
        var mcd = columnPathFor(BookEntity.class, "title");

        assertThat(mcd.column().name())
                .as("db-side name is the source of truth for SQL rendering — it must mirror the @Column name from the field descriptor")
                .isEqualTo("title")
                .isEqualTo(mcd.field().columnName());
    }

    @Test
    void dbColumnDescriptorForStringColumnExposesVarcharSqlsqlType() {
        var mcd = columnPathFor(BookEntity.class, "title");

        assertThat(mcd.column().sqlType())
                .as("a String leaf column maps to SQL VARCHAR — needed by DDL generation and JDBC binding")
                .isEqualTo(JDBCType.VARCHAR);
    }

    @Test
    void dbColumnDescriptorForLongColumnExposesBigintSqlsqlType() {
        var mcd = columnPathFor(BookEntity.class, "page_count");

        assertThat(mcd.column().sqlType())
                .as("a Long leaf column maps to SQL BIGINT")
                .isEqualTo(JDBCType.BIGINT);
    }

    @Test
    void embeddedLeafColumnExposesDbColumnDescriptor() {
        var mcd = columnPathFor(UserFixtureEntity.class, "first_name");

        assertThat(mcd.column())
                .as("every leaf column — including those flattened out of an embeddable — must carry its db-side descriptor")
                .isNotNull();
        assertThat(mcd.column().name()).isEqualTo("first_name");
        assertThat(mcd.column().sqlType()).isEqualTo(JDBCType.VARCHAR);
    }

    @Test
    void deeplyNestedEmbeddedLeafColumnExposesDbColumnDescriptor() {
        var mcd = columnPathFor(UserFixtureEntity.class, "title_before");

        assertThat(mcd.column())
                .as("nested-embeddable leaf columns are rendered into SQL just like top-level ones — their db descriptor must be populated too")
                .isNotNull();
        assertThat(mcd.column().name()).isEqualTo("title_before");
        assertThat(mcd.column().sqlType()).isEqualTo(JDBCType.VARCHAR);
    }

    @Test
    void scalarIdColumnExposesDbColumnDescriptor() {
        var mcd = idPathFor(BookEntity.class, "id");

        assertThat(mcd.column())
                .as("the id path drives find/delete WHERE clauses — its db descriptor must be populated, not null")
                .isNotNull();
        assertThat(mcd.column().name()).isEqualTo("id");
        assertThat(mcd.column().sqlType()).isEqualTo(JDBCType.BIGINT);
    }

    @Test
    void embeddedIdKeyColumnExposesDbColumnDescriptor() {
        var mcd = idPathFor(CompositeKeyFixtureEntity.class, "part_a");

        assertThat(mcd.column())
                .as("each part of an embedded id ends up in SQL as its own column — its db descriptor must be populated")
                .isNotNull();
        assertThat(mcd.column().name()).isEqualTo("part_a");
    }

    @Test
    void managedColumnDescriptorFieldIsAlwaysPopulated() {
        var mcd = columnPathFor(BookEntity.class, "title");

        assertThat(mcd.field())
                .as("the code-side field descriptor is the entry point for reflection — it must never be null for a leaf column")
                .isNotNull();
    }

    @Test
    void managedColumnDescriptorEntityPathIsAlwaysPopulated() {
        var mcd = columnPathFor(BookEntity.class, "title");

        assertThat(mcd.entityPath())
                .as("the entity-rooted path is how read/write reach the leaf — it must never be null for a leaf column")
                .isNotNull();
    }
}
