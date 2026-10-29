package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.fixtures.TestDescriptors;
import cz.tstrecha.foundry.orm.fixtures.embedded.CompositeKeyFixtureEntity;
import cz.tstrecha.foundry.orm.fixtures.embedded.UserFixtureEntity;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PathFinderTest {

    @Test
    void buildColumnPathsExposesOnePathPerLeafColumnForScalarEntity() {
        var paths = PathFinder.buildColumnPaths(TestDescriptors.descriptorFor(BookEntity.class));

        assertThat(paths).extracting(cp -> cp.fieldDescriptor().columnName())
                .containsExactlyInAnyOrder("id", "title", "author", "genre", "page_count");
    }

    @Test
    void topLevelColumnPathHasEmptyFieldPath() {
        var paths = PathFinder.buildColumnPaths(TestDescriptors.descriptorFor(BookEntity.class));

        assertThat(paths).allSatisfy(cp ->
                assertThat(cp.fieldPath().nodes())
                        .as("top-level @Column fields produce a path with no intermediate nodes")
                        .isEmpty());
    }

    @Test
    void buildColumnPathsFlattensEmbeddedChildren() {
        var paths = PathFinder.buildColumnPaths(TestDescriptors.descriptorFor(UserFixtureEntity.class));

        assertThat(paths).extracting(cp -> cp.fieldDescriptor().columnName())
                .containsExactlyInAnyOrder("id", "company_name", "first_name", "last_name", "title_before", "title_after");
    }

    @Test
    void pathForOneLevelEmbeddedColumnHasSingleIntermediateNode() {
        var paths = PathFinder.buildColumnPaths(TestDescriptors.descriptorFor(UserFixtureEntity.class));

        var firstName = paths.stream()
                .filter(p -> "first_name".equals(p.fieldDescriptor().columnName()))
                .findFirst().orElseThrow();

        assertThat(firstName.fieldPath().nodes())
                .as("first_name is reached via UserFixtureEntity.name — exactly one intermediate node")
                .hasSize(1);
        assertThat(firstName.fieldPath().nodes().get(0).field().getName()).isEqualTo("name");
    }

    @Test
    void pathForNestedEmbeddedColumnHasTwoIntermediateNodes() {
        var paths = PathFinder.buildColumnPaths(TestDescriptors.descriptorFor(UserFixtureEntity.class));

        var titleBefore = paths.stream()
                .filter(p -> "title_before".equals(p.fieldDescriptor().columnName()))
                .findFirst().orElseThrow();

        assertThat(titleBefore.fieldPath().nodes())
                .extracting(n -> n.field().getName())
                .as("title_before is reached via name -> titles")
                .containsExactly("name", "titles");
    }

    @Test
    void allEntityRootedPathsAreMarkedAsNonRootValue() {
        var paths = PathFinder.buildColumnPaths(TestDescriptors.descriptorFor(UserFixtureEntity.class));

        assertThat(paths).allSatisfy(cp -> assertThat(cp.rootValue())
                .as("paths produced by buildColumnPaths are always entity-rooted — root is never the value itself")
                .isFalse());
    }

    @Test
    void buildIdPathsForScalarIdReturnsSinglePathWithRootValueMarker() {
        var paths = PathFinder.buildIdPaths(TestDescriptors.descriptorFor(BookEntity.class));

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).rootValue())
                .as("a scalar @Id value IS the column value — nothing to walk into")
                .isTrue();
        assertThat(paths.get(0).fieldPath().nodes()).isEmpty();
        assertThat(paths.get(0).fieldDescriptor().columnName()).isEqualTo("id");
    }

    @Test
    void buildIdPathsForEmbeddedIdReturnsPathPerKeyColumn() {
        var paths = PathFinder.buildIdPaths(TestDescriptors.descriptorFor(CompositeKeyFixtureEntity.class));

        assertThat(paths).extracting(cp -> cp.fieldDescriptor().columnName())
                .containsExactlyInAnyOrder("part_a", "part_b");
    }

    @Test
    void embeddedIdPathsHaveEmptyFieldPathBecauseRootIsTheKeyInstance() {
        var paths = PathFinder.buildIdPaths(TestDescriptors.descriptorFor(CompositeKeyFixtureEntity.class));

        assertThat(paths).allSatisfy(cp -> assertThat(cp.fieldPath().nodes())
                .as("for an embedded id, root is already the key instance — no intermediate traversal needed")
                .isEmpty());
    }

    @Test
    void embeddedIdPathsAreNotMarkedAsRootValue() {
        var paths = PathFinder.buildIdPaths(TestDescriptors.descriptorFor(CompositeKeyFixtureEntity.class));

        assertThat(paths).allSatisfy(cp -> assertThat(cp.rootValue())
                .as("embedded id columns require one leaf field access from the key instance — root is not the value")
                .isFalse());
    }

    @Test
    void buildIdPathsForScalarIdLeafFieldPointsToTheIdField() {
        var paths = PathFinder.buildIdPaths(TestDescriptors.descriptorFor(BookEntity.class));

        assertThat(paths.get(0).fieldDescriptor().field().getName()).isEqualTo("id");
    }

    @Test
    void buildColumnPathsWithNullEntityDescriptorIsRejected() {
        assertThatThrownBy(() -> PathFinder.buildColumnPaths(null))
                .as("the public entry point must reject null — bad input is a caller bug, not a silent NPE deep in the walker")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buildIdPathsWithNullEntityDescriptorIsRejected() {
        assertThatThrownBy(() -> PathFinder.buildIdPaths(null))
                .as("the public entry point must reject null — bad input is a caller bug, not a silent NPE deep in the walker")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void entityRootedAndIdRootedPathsForSameColumnReferenceSameLeafField() {
        var columnPaths = PathFinder.buildColumnPaths(TestDescriptors.descriptorFor(CompositeKeyFixtureEntity.class));
        var idPaths = PathFinder.buildIdPaths(TestDescriptors.descriptorFor(CompositeKeyFixtureEntity.class));

        var partAColumnPath = columnPaths.stream()
                .filter(p -> "part_a".equals(p.fieldDescriptor().columnName()))
                .findFirst().orElseThrow();
        var partAIdPath = idPaths.stream()
                .filter(p -> "part_a".equals(p.fieldDescriptor().columnName()))
                .findFirst().orElseThrow();

        assertThat(partAColumnPath.fieldDescriptor().field())
                .as("both views describe the same leaf field, only the root differs")
                .isEqualTo(partAIdPath.fieldDescriptor().field());
    }
}
