package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.fixtures.TestDescriptors;
import cz.tstrecha.foundry.orm.fixtures.embedded.CompositeKeyFixtureEntity;
import cz.tstrecha.foundry.orm.fixtures.embedded.UserFixtureEntity;
import cz.tstrecha.foundry.orm.fixtures.generated.SequencedWidgetEntity;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import cz.tstrecha.foundry.orm.fixtures.pathfinderunknowngen.UnknownGeneratorEntity;
import cz.tstrecha.foundry.orm.fixtures.uuidgen.UuidWidgetEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PathFinderTest {

    @Test
    void buildColumnPathsExposesOnePathPerLeafColumnForScalarEntity() {
        var paths = TestDescriptors.columnPathsFor(BookEntity.class);

        assertThat(paths).extracting(cp -> cp.field().columnName())
                .containsExactlyInAnyOrder("id", "title", "author", "genre", "page_count");
    }

    @Test
    void topLevelColumnPathHasEmptyFieldPath() {
        var paths = TestDescriptors.columnPathsFor(BookEntity.class);

        assertThat(paths).allSatisfy(cp ->
                assertThat(cp.entityPath().fieldPath().nodes())
                        .as("top-level @Column fields produce a path with no intermediate nodes")
                        .isEmpty());
    }

    @Test
    void buildColumnPathsFlattensEmbeddedChildren() {
        var paths = TestDescriptors.columnPathsFor(UserFixtureEntity.class);

        assertThat(paths).extracting(cp -> cp.field().columnName())
                .containsExactlyInAnyOrder("id", "company_name", "first_name", "last_name", "title_before", "title_after");
    }

    @Test
    void pathForOneLevelEmbeddedColumnHasSingleIntermediateNode() {
        var paths = TestDescriptors.columnPathsFor(UserFixtureEntity.class);

        var firstName = paths.stream()
                .filter(p -> "first_name".equals(p.field().columnName()))
                .findFirst().orElseThrow();

        assertThat(firstName.entityPath().fieldPath().nodes())
                .as("first_name is reached via UserFixtureEntity.name — exactly one intermediate node")
                .hasSize(1);
        assertThat(firstName.entityPath().fieldPath().nodes().get(0).field().getName()).isEqualTo("name");
    }

    @Test
    void pathForNestedEmbeddedColumnHasTwoIntermediateNodes() {
        var paths = TestDescriptors.columnPathsFor(UserFixtureEntity.class);

        var titleBefore = paths.stream()
                .filter(p -> "title_before".equals(p.field().columnName()))
                .findFirst().orElseThrow();

        assertThat(titleBefore.entityPath().fieldPath().nodes())
                .extracting(n -> n.field().getName())
                .as("title_before is reached via name -> titles")
                .containsExactly("name", "titles");
    }

    @Test
    void allEntityRootedPathsAreMarkedAsNonRootValue() {
        var paths = TestDescriptors.columnPathsFor(UserFixtureEntity.class);

        assertThat(paths).allSatisfy(cp -> assertThat(cp.entityPath().rootValue())
                .as("paths produced by buildColumnPaths are always entity-rooted — root is never the value itself")
                .isFalse());
    }

    @Test
    void buildIdPathsForScalarIdReturnsSinglePathWithRootValueMarker() {
        var paths = TestDescriptors.idPathsFor(BookEntity.class);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).entityPath().rootValue())
                .as("a scalar @Id value IS the column value — nothing to walk into")
                .isTrue();
        assertThat(paths.get(0).entityPath().fieldPath().nodes()).isEmpty();
        assertThat(paths.get(0).field().columnName()).isEqualTo("id");
    }

    @Test
    void buildIdPathsForEmbeddedIdReturnsPathPerKeyColumn() {
        var paths = TestDescriptors.idPathsFor(CompositeKeyFixtureEntity.class);

        assertThat(paths).extracting(cp -> cp.field().columnName())
                .containsExactlyInAnyOrder("part_a", "part_b");
    }

    @Test
    void embeddedIdPathsHaveEmptyFieldPathBecauseRootIsTheKeyInstance() {
        var paths = TestDescriptors.idPathsFor(CompositeKeyFixtureEntity.class);

        assertThat(paths).allSatisfy(cp -> assertThat(cp.entityPath().fieldPath().nodes())
                .as("for an embedded id, root is already the key instance — no intermediate traversal needed")
                .isEmpty());
    }

    @Test
    void embeddedIdPathsAreNotMarkedAsRootValue() {
        var paths = TestDescriptors.idPathsFor(CompositeKeyFixtureEntity.class);

        assertThat(paths).allSatisfy(cp -> assertThat(cp.entityPath().rootValue())
                .as("embedded id columns require one leaf field access from the key instance — root is not the value")
                .isFalse());
    }

    @Test
    void buildIdPathsForScalarIdLeafFieldPointsToTheIdField() {
        var paths = TestDescriptors.idPathsFor(BookEntity.class);

        assertThat(paths.get(0).field().field().getName()).isEqualTo("id");
    }

    @Test
    void buildColumnPathsWithNullEntityDescriptorIsRejected() {
        var registry = TestDescriptors.descriptorAndRegistryFor(BookEntity.class).registry();
        assertThatThrownBy(() -> PathFinder.buildColumnPaths(null, registry))
                .as("the public entry point must reject null — bad input is a caller bug, not a silent NPE deep in the walker")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buildIdPathsWithNullEntityDescriptorIsRejected() {
        var registry = TestDescriptors.descriptorAndRegistryFor(BookEntity.class).registry();
        assertThatThrownBy(() -> PathFinder.buildIdPaths(null, registry))
                .as("the public entry point must reject null — bad input is a caller bug, not a silent NPE deep in the walker")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void columnWithoutGeneratedValueCarriesNoSequenceDescriptor() {
        var paths = TestDescriptors.columnPathsFor(BookEntity.class);

        assertThat(paths)
                .as("none of BookEntity's columns use @GeneratedValue — their ManagedColumnDescriptor must carry no sequence metadata")
                .allSatisfy(p -> assertThat(p.sequenceDescriptor()).isNull());
    }

    @Test
    void scalarIdWithoutGeneratedValueCarriesNoSequenceDescriptorInIdPath() {
        var paths = TestDescriptors.idPathsFor(BookEntity.class);

        assertThat(paths.get(0).sequenceDescriptor())
                .as("a caller-assigned @Id has no generator — its id path must not advertise one")
                .isNull();
    }

    @Test
    void columnWithGeneratedValueCarriesResolvedSequenceInManagedColumnDescriptor() {
        var idPath = TestDescriptors.columnPathsFor(SequencedWidgetEntity.class).stream()
                .filter(p -> "id".equals(p.field().columnName()))
                .findFirst().orElseThrow();

        assertThat(idPath.sequenceDescriptor())
                .as("resolution happens at descriptor build time — the ManagedColumnDescriptor must already carry the SequenceDescriptor so runtime does not need the registry")
                .isNotNull();
        assertThat(idPath.sequenceDescriptor().name()).isEqualTo("widget_seq_gen");
        assertThat(idPath.sequenceDescriptor().sequenceName()).isEqualTo("widget_seq");
    }

    @Test
    void scalarIdWithGeneratedValueCarriesResolvedSequenceInIdPath() {
        var paths = TestDescriptors.idPathsFor(SequencedWidgetEntity.class);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).sequenceDescriptor())
                .as("the id path drives value generation during persist — it must carry the resolved SequenceDescriptor just like the entity-rooted path does")
                .isNotNull();
        assertThat(paths.get(0).sequenceDescriptor().sequenceName()).isEqualTo("widget_seq");
    }

    @Test
    void generatedValueReferencingUnknownGeneratorIsRejected() {
        assertThatThrownBy(() -> TestDescriptors.columnPathsFor(UnknownGeneratorEntity.class))
                .as("@GeneratedValue(generator = \"...\") that points at no declared @SequenceGenerator is a misconfiguration — must fail fast at descriptor build time, not later with an NPE in SequencePersister")
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void generatedValueReferencingUnknownGeneratorIsRejectedForIdPathToo() {
        assertThatThrownBy(() -> TestDescriptors.idPathsFor(UnknownGeneratorEntity.class))
                .as("unknown-generator contract must apply regardless of which walk discovered the column — id paths carry the same risk")
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void columnWithUuidGeneratedValueCarriesNoSequenceDescriptor() {
        var idPath = TestDescriptors.columnPathsFor(UuidWidgetEntity.class).stream()
                .filter(p -> "id".equals(p.field().columnName()))
                .findFirst().orElseThrow();

        assertThat(idPath.sequenceDescriptor())
                .as("UUID is a client-side strategy — no sequence to resolve, so the resolver must early-exit on strategy != SEQUENCE instead of hunting for a nonexistent entry")
                .isNull();
    }

    @Test
    void scalarIdWithUuidStrategyCarriesNoSequenceDescriptorInIdPath() {
        var paths = TestDescriptors.idPathsFor(UuidWidgetEntity.class);

        assertThat(paths).hasSize(1);
        assertThat(paths.get(0).sequenceDescriptor())
                .as("the id path must mirror the column path — UUID strategy leaves it empty on both views, so runtime generator wiring is consistent")
                .isNull();
    }

    @Test
    void uuidGeneratedValueDoesNotRequireAnyRegisteredSequence() {
        var paths = TestDescriptors.columnPathsFor(UuidWidgetEntity.class);

        assertThat(paths)
                .as("a UUID @GeneratedValue has no @SequenceGenerator anywhere in sight — descriptor build must succeed with an empty sequences map, not fail looking for one")
                .isNotEmpty();
    }

    @Test
    void entityRootedAndIdRootedPathsForSameColumnReferenceSameLeafField() {
        var columnPaths = TestDescriptors.columnPathsFor(CompositeKeyFixtureEntity.class);
        var idPaths = TestDescriptors.idPathsFor(CompositeKeyFixtureEntity.class);

        var partAColumnPath = columnPaths.stream()
                .filter(p -> "part_a".equals(p.field().columnName()))
                .findFirst().orElseThrow();
        var partAIdPath = idPaths.stream()
                .filter(p -> "part_a".equals(p.field().columnName()))
                .findFirst().orElseThrow();

        assertThat(partAColumnPath.field().field())
                .as("both views describe the same leaf field, only the root differs")
                .isEqualTo(partAIdPath.field().field());
    }
}
