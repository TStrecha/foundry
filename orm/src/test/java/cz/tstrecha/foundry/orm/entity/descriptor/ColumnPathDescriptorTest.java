package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.fixtures.TestDescriptors;
import cz.tstrecha.foundry.orm.fixtures.embedded.CompositeKeyFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.CompositeKeyFixtureEntity;
import cz.tstrecha.foundry.orm.fixtures.embedded.NameFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.TitlesFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.UserFixtureEntity;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ColumnPathDescriptorTest {

    private ColumnPathDescriptor<?> columnPathFor(Class<?> entity, String columnName) {
        return PathFinder.buildColumnPaths(TestDescriptors.descriptorFor(entity)).stream()
                .filter(p -> columnName.equals(p.field().columnName()))
                .findFirst().orElseThrow()
                .entityPath();
    }

    private ColumnPathDescriptor<?> idPathFor(Class<?> entity, String columnName) {
        return PathFinder.buildIdPaths(TestDescriptors.descriptorFor(entity)).stream()
                .filter(p -> columnName.equals(p.field().columnName()))
                .findFirst().orElseThrow()
                .entityPath();
    }

    @Test
    void readValueFromRootReadsTopLevelColumnDirectly() {
        var titlePath = columnPathFor(BookEntity.class, "title");
        var book = new BookEntity();
        book.setTitle("Dune");

        assertThat(titlePath.readValueFromRoot(book)).isEqualTo("Dune");
    }

    @Test
    void readValueFromRootWalksOneLevelEmbeddedToReachValue() {
        var firstNamePath = columnPathFor(UserFixtureEntity.class, "first_name");
        var user = new UserFixtureEntity();
        user.setName(new NameFixture(null, "Alice", "Smith"));

        assertThat(firstNamePath.readValueFromRoot(user)).isEqualTo("Alice");
    }

    @Test
    void readValueFromRootWalksTwoLevelsForNestedEmbedded() {
        var titleBeforePath = columnPathFor(UserFixtureEntity.class, "title_before");
        var user = new UserFixtureEntity();
        user.setName(new NameFixture(new TitlesFixture("Dr.", "PhD"), "Alice", "Smith"));

        assertThat(titleBeforePath.readValueFromRoot(user)).isEqualTo("Dr.");
    }

    @Test
    void readValueFromRootReturnsNullWhenIntermediateEmbeddedIsNull() {
        var firstNamePath = columnPathFor(UserFixtureEntity.class, "first_name");
        var user = new UserFixtureEntity();

        assertThat(firstNamePath.readValueFromRoot(user))
                .as("null intermediate embeddable must short-circuit without NPE")
                .isNull();
    }

    @Test
    void readValueFromRootReturnsNullWhenDeepIntermediateEmbeddedIsNull() {
        var titleBeforePath = columnPathFor(UserFixtureEntity.class, "title_before");
        var user = new UserFixtureEntity();
        user.setName(new NameFixture(null, "A", "B"));

        assertThat(titleBeforePath.readValueFromRoot(user))
                .as("intermediate null must short-circuit even when path has multiple nodes")
                .isNull();
    }

    @Test
    void readValueFromRootWithRootValueMarkerReturnsRootAsValue() {
        var idPath = idPathFor(BookEntity.class, "id");

        assertThat(idPath.readValueFromRoot(42L))
                .as("scalar id-rooted path: the Long value IS the column value, nothing to walk")
                .isEqualTo(42L);
    }

    @Test
    void readValueFromRootOnEmbeddedIdReadsFromKeyInstance() {
        var partAPath = idPathFor(CompositeKeyFixtureEntity.class, "part_a");
        var key = new CompositeKeyFixture("A-value", "B-value");

        assertThat(partAPath.readValueFromRoot(key)).isEqualTo("A-value");
    }

    @Test
    void writeValueToRootWritesTopLevelColumnDirectly() {
        var titlePath = columnPathFor(BookEntity.class, "title");
        var book = new BookEntity();

        titlePath.writeValueToRoot(book, "Hyperion");

        assertThat(book.getTitle()).isEqualTo("Hyperion");
    }

    @Test
    void writeValueToRootAutoInstantiatesMissingEmbeddedOnTheFly() {
        var firstNamePath = columnPathFor(UserFixtureEntity.class, "first_name");
        var user = new UserFixtureEntity();

        firstNamePath.writeValueToRoot(user, "Alice");

        assertThat(user.getName())
                .as("writing a nested column must create the missing embeddable rather than silently dropping the value")
                .isNotNull();
        assertThat(user.getName().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void writeValueToRootAutoInstantiatesAllMissingIntermediatesInDeepPath() {
        var titleBeforePath = columnPathFor(UserFixtureEntity.class, "title_before");
        var user = new UserFixtureEntity();

        titleBeforePath.writeValueToRoot(user, "Dr.");

        assertThat(user.getName()).isNotNull();
        assertThat(user.getName().getTitles()).isNotNull();
        assertThat(user.getName().getTitles().getTitleBefore()).isEqualTo("Dr.");
    }

    @Test
    void writeValueToRootPreservesExistingEmbeddedWhenPresent() {
        var firstNamePath = columnPathFor(UserFixtureEntity.class, "first_name");
        var existingName = new NameFixture(new TitlesFixture("Dr.", "PhD"), "old", "last");
        var user = new UserFixtureEntity();
        user.setName(existingName);

        firstNamePath.writeValueToRoot(user, "Alice");

        assertThat(user.getName())
                .as("existing embeddable instance must not be replaced on write")
                .isSameAs(existingName);
        assertThat(user.getName().getFirstName()).isEqualTo("Alice");
        assertThat(user.getName().getLastName()).isEqualTo("last");
    }

    @Test
    void writeValueToRootOnRootValuePathIsRejected() {
        var idPath = idPathFor(BookEntity.class, "id");

        assertThatThrownBy(() -> idPath.writeValueToRoot(42L, 99L))
                .as("rootValue=true means the root IS the column value — there is nothing writable to update, writes must be explicitly rejected")
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void readValueFromRootWithNullRootForEntityRootedPathIsRejected() {
        var titlePath = columnPathFor(BookEntity.class, "title");

        assertThatThrownBy(() -> titlePath.readValueFromRoot(null))
                .as("entity-rooted read with null root is a caller bug — must be rejected explicitly, not silently return null")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void writeValueToRootWithNullRootIsRejected() {
        var titlePath = columnPathFor(BookEntity.class, "title");

        assertThatThrownBy(() -> titlePath.writeValueToRoot(null, "Dune"))
                .as("writing into a null root cannot succeed — must be rejected explicitly")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void writeValueToRootAcceptsNullValueAsSqlNullSemantics() {
        var titlePath = columnPathFor(BookEntity.class, "title");
        var book = new BookEntity();
        book.setTitle("existing");

        titlePath.writeValueToRoot(book, null);

        assertThat(book.getTitle())
                .as("writing null is a legitimate operation — SQL NULL maps to Java null")
                .isNull();
    }
}
