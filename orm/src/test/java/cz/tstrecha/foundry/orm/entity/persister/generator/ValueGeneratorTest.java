package cz.tstrecha.foundry.orm.entity.persister.generator;

import cz.tstrecha.foundry.orm.entity.descriptor.code.GeneratorDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.db.SequenceDescriptor;
import jakarta.persistence.GenerationType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueGeneratorTest {

    private static final SequenceDescriptor WIDGET_SEQ =
            new SequenceDescriptor("widget_seq_gen", "widget_seq");

    @Test
    void nullGeneratorDescriptorYieldsNoGenerator() {
        var generator = ValueGenerator.fromGeneratorDescriptor(null, WIDGET_SEQ);

        assertThat(generator)
                .as("a column without @GeneratedValue has no generator — the factory must say so with null, not fabricate a no-op strategy")
                .isNull();
    }

    @Test
    void sequenceStrategyProducesASequenceGenerator() {
        var descriptor = new GeneratorDescriptor(GenerationType.SEQUENCE, "widget_seq_gen");

        var generator = ValueGenerator.fromGeneratorDescriptor(descriptor, WIDGET_SEQ);

        assertThat(generator)
                .as("SEQUENCE strategy must dispatch to the sequence-backed implementation")
                .isInstanceOf(SequenceGenerator.class);
    }

    @Test
    void uuidStrategyProducesAUuidGenerator() {
        var descriptor = new GeneratorDescriptor(GenerationType.UUID, "");

        var generator = ValueGenerator.fromGeneratorDescriptor(descriptor, null);

        assertThat(generator)
                .as("UUID strategy must dispatch to the client-side UUID implementation — the sequence descriptor is irrelevant here and may be null")
                .isInstanceOf(UUIDGenerator.class);
    }

    @Test
    void uuidStrategyDoesNotRequireASequenceDescriptor() {
        var descriptor = new GeneratorDescriptor(GenerationType.UUID, "");

        var generator = ValueGenerator.fromGeneratorDescriptor(descriptor, null);

        assertThat(generator)
                .as("UUID is a client-side strategy — passing a null sequence descriptor must be a legitimate call, never a cause of failure")
                .isNotNull();
    }

    @Test
    void identityStrategyIsRejectedAsUnsupportedUntilImplemented() {
        var descriptor = new GeneratorDescriptor(GenerationType.IDENTITY, "");

        assertThatThrownBy(() -> ValueGenerator.fromGeneratorDescriptor(descriptor, null))
                .as("an unimplemented strategy must announce itself with UnsupportedOperationException — IllegalStateException reads as internal corruption, which is the wrong signal to the caller")
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void tableStrategyIsRejectedAsUnsupportedUntilImplemented() {
        var descriptor = new GeneratorDescriptor(GenerationType.TABLE, "");

        assertThatThrownBy(() -> ValueGenerator.fromGeneratorDescriptor(descriptor, null))
                .as("same contract for TABLE — missing support must be signalled with UnsupportedOperationException, not with an internal-state exception")
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void autoStrategyIsRejectedAsUnsupportedUntilImplemented() {
        var descriptor = new GeneratorDescriptor(GenerationType.AUTO, "");

        assertThatThrownBy(() -> ValueGenerator.fromGeneratorDescriptor(descriptor, null))
                .as("AUTO delegates to the provider's choice — until Foundry picks a default, the strategy must throw UnsupportedOperationException with a message that says which strategy was requested")
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
