package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.entity.descriptor.code.EntityDescriptor;
import cz.tstrecha.foundry.orm.entity.system.ColumnsEntity;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import cz.tstrecha.foundry.orm.fixtures.registryscan.ClassLevelSequenceEntity;
import cz.tstrecha.foundry.orm.fixtures.registryscan.FieldLevelSequenceEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DescriptorRegistryTest {

    @Test
    void nullScanningRootsIsRejected() {
        assertThatThrownBy(() -> new DescriptorRegistry(null))
                .as("construction must reject null scanning roots explicitly — anything else is a silent configuration bug surfaced as NPE much later")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emptyScanningRootsStillRegistersBuiltInSystemEntities() {
        var registry = new DescriptorRegistry(List.of());

        assertThat(registry.getTypes())
                .as("framework ships its own built-in entities (ColumnsEntity et al.) — these must be scanned even when the caller passes no roots, so core features keep working out of the box")
                .containsKey(ColumnsEntity.class);
    }

    @Test
    void emptyScanningRootsDoNotPullInApplicationEntities() {
        var registry = new DescriptorRegistry(List.of());

        assertThat(registry.getTypes())
                .as("without an explicit root, nothing outside the framework's own packages is reachable — the scanner must not leak into fixtures/good")
                .doesNotContainKey(BookEntity.class);
    }

    @Test
    void sequencesMapIsNeverNullSoCallersCanIterateWithoutGuard() {
        var registry = new DescriptorRegistry(List.of());

        assertThat(registry.getSequences())
                .as("the sequences map is part of the stable registry contract — must be an empty map, never null, so lookup code does not need a null-guard")
                .isNotNull();
    }

    @Test
    void scanningPicksUpEntityIntoTypesMap() {
        var registry = new DescriptorRegistry(List.of(ClassLevelSequenceEntity.class));

        assertThat(registry.getTypes())
                .as("an entity in a scanned package must be reachable from the registry's types map keyed by its own Class")
                .containsKey(ClassLevelSequenceEntity.class);
        assertThat(registry.getTypes().get(ClassLevelSequenceEntity.class))
                .isInstanceOf(EntityDescriptor.class);
    }

    @Test
    void classLevelSequenceGeneratorIsScannedIntoSequencesMap() {
        var registry = new DescriptorRegistry(List.of(ClassLevelSequenceEntity.class));

        assertThat(registry.getSequences())
                .as("a @SequenceGenerator sitting on the entity class is a legitimate JPA placement — the scanner must find it there")
                .containsKey("class_level_seq");
    }

    @Test
    void fieldLevelSequenceGeneratorIsScannedIntoSequencesMap() {
        var registry = new DescriptorRegistry(List.of(FieldLevelSequenceEntity.class));

        assertThat(registry.getSequences())
                .as("a @SequenceGenerator sitting next to the @Id field is the other legitimate JPA placement — the scanner must find it there too")
                .containsKey("field_level_seq");
    }

    @Test
    void scannedSequenceDescriptorCarriesBothGeneratorNameAndSequenceName() {
        var registry = new DescriptorRegistry(List.of(FieldLevelSequenceEntity.class));

        var seq = registry.getSequences().get("field_level_seq");
        assertThat(seq.name())
                .as("the JPA-side generator alias — what @GeneratedValue(generator=...) refers to — must be preserved")
                .isEqualTo("field_level_seq");
        assertThat(seq.sequenceName())
                .as("the physical db-side sequence name — what SELECT nextval(...) targets — must be preserved separately from the alias")
                .isEqualTo("field_level_seqdb");
    }

    @Test
    void entityWithoutAnyGeneratorYieldsEmptySequencesMap() {
        var registry = new DescriptorRegistry(List.of(BookEntity.class));

        assertThat(registry.getSequences())
                .as("BookEntity declares no @SequenceGenerator anywhere — scan must leave the sequences map empty, not populate it with a stray default")
                .isEmpty();
    }
}
