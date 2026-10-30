package cz.tstrecha.foundry.orm.entity.descriptor.code;

import cz.tstrecha.foundry.orm.fixtures.generated.SequencedWidgetEntity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratorDescriptorTest {

    @Test
    void primaryConstructorCarriesStrategyAndNameVerbatim() {
        var descriptor = new GeneratorDescriptor(GenerationType.SEQUENCE, "widget_seq_gen");

        assertThat(descriptor.strategy()).isEqualTo(GenerationType.SEQUENCE);
        assertThat(descriptor.name()).isEqualTo("widget_seq_gen");
    }

    @Test
    void copyFromAnnotationMapsStrategyAndGeneratorAliasToTheDescriptor() throws NoSuchFieldException {
        var annotation = SequencedWidgetEntity.class.getDeclaredField("id").getAnnotation(GeneratedValue.class);

        var descriptor = new GeneratorDescriptor(annotation);

        assertThat(descriptor.strategy())
                .as("strategy is what tells the persist pipeline which generation path to take — must round-trip verbatim")
                .isEqualTo(GenerationType.SEQUENCE);
        assertThat(descriptor.name())
                .as("the generator alias from @GeneratedValue(generator=...) is the lookup key into the sequences registry — must round-trip verbatim")
                .isEqualTo("widget_seq_gen");
    }
}
