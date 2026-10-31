package cz.tstrecha.foundry.orm.entity.persister.generator;

import cz.tstrecha.foundry.orm.entity.descriptor.code.GeneratorDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.db.SequenceDescriptor;
import jakarta.persistence.GenerationType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PreInsertValueGeneratorTest {

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
    void identityStrategyProducesAnIdentityGenerator() {
        var descriptor = new GeneratorDescriptor(GenerationType.IDENTITY, "");

        var generator = ValueGenerator.fromGeneratorDescriptor(descriptor, null);

        assertThat(generator)
                .as("IDENTITY strategy must dispatch to the post-insert IdentityGenerator — the sequence descriptor is irrelevant because the value is produced by the database during INSERT, not by a sequence lookup beforehand")
                .isInstanceOf(IdentityGenerator.class);
    }

    @Test
    void identityStrategyDoesNotRequireASequenceDescriptor() {
        var descriptor = new GeneratorDescriptor(GenerationType.IDENTITY, "");

        var generator = ValueGenerator.fromGeneratorDescriptor(descriptor, null);

        assertThat(generator)
                .as("IDENTITY is a database-side strategy — passing a null sequence descriptor must be a legitimate call, never a cause of failure")
                .isNotNull();
    }

    @Test
    void tableStrategyIsRejectedAsUnsupportedUntilImplemented() {
        var descriptor = new GeneratorDescriptor(GenerationType.TABLE, "");

        assertThatThrownBy(() -> ValueGenerator.fromGeneratorDescriptor(descriptor, null))
                .as("TABLE is a strategy Foundry does not implement yet (hi-lo table backing a shared sequence) — the factory must signal 'not supported', not pass through with a null generator which would NPE at bind time")
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void tableStrategyRejectionMentionsTheStrategyName() {
        var descriptor = new GeneratorDescriptor(GenerationType.TABLE, "");

        assertThatThrownBy(() -> ValueGenerator.fromGeneratorDescriptor(descriptor, null))
                .as("the error must name the strategy that was asked for — otherwise, when it fires through a scan over many entities, the user cannot tell which @GeneratedValue triggered it")
                .hasMessageContaining("TABLE");
    }

    @Test
    void autoStrategyIsRejectedBecauseConcreteStrategyMustBeDeclaredExplicitly() {
        var descriptor = new GeneratorDescriptor(GenerationType.AUTO, "");

        assertThatThrownBy(() -> ValueGenerator.fromGeneratorDescriptor(descriptor, null))
                .as("AUTO is rejected as a deliberate design decision, not a TODO — Foundry cannot verify the DB schema matches the strategy it would pick, so the caller must declare IDENTITY/SEQUENCE/UUID themselves. The failure must be an IllegalStateException (caller configuration bug) rather than UnsupportedOperationException (framework gap)")
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void autoStrategyRejectionMessageDirectsTheUserToPickAConcreteStrategy() {
        var descriptor = new GeneratorDescriptor(GenerationType.AUTO, "");

        assertThatThrownBy(() -> ValueGenerator.fromGeneratorDescriptor(descriptor, null))
                .as("the message must tell the user what to do next (pick a concrete strategy), not just complain — AUTO is a common default annotation choice and the message is the only clue callers get at scan time")
                .hasMessageContaining("AUTO");
    }

    @Test
    void autoAndTableRejectionsCarryDistinctExceptionTypes() {
        var autoDescriptor = new GeneratorDescriptor(GenerationType.AUTO, "");
        var tableDescriptor = new GeneratorDescriptor(GenerationType.TABLE, "");

        assertThatThrownBy(() -> ValueGenerator.fromGeneratorDescriptor(autoDescriptor, null))
                .as("AUTO is a configuration bug on the caller's side — IllegalStateException signals 'your @GeneratedValue is wrong', which is actionable")
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> ValueGenerator.fromGeneratorDescriptor(tableDescriptor, null))
                .as("TABLE is a framework gap — UnsupportedOperationException signals 'this ORM does not implement this yet', which is a different fix than reconfiguring an annotation")
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
