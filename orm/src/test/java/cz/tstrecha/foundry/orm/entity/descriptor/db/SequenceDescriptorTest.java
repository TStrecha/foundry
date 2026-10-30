package cz.tstrecha.foundry.orm.entity.descriptor.db;

import cz.tstrecha.foundry.orm.fixtures.generated.SequencedWidgetEntity;
import jakarta.persistence.SequenceGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SequenceDescriptorTest {

    @Test
    void primaryConstructorCarriesNameAndSequenceNameVerbatim() {
        var descriptor = new SequenceDescriptor("widget_seq_gen", "widget_seq");

        assertThat(descriptor.name()).isEqualTo("widget_seq_gen");
        assertThat(descriptor.sequenceName()).isEqualTo("widget_seq");
    }

    @Test
    void copyFromAnnotationMapsBothFields() throws NoSuchFieldException {
        var annotation = SequencedWidgetEntity.class.getDeclaredField("id").getAnnotation(SequenceGenerator.class);

        var descriptor = new SequenceDescriptor(annotation);

        assertThat(descriptor.name())
                .as("the alias — @SequenceGenerator.name — is what @GeneratedValue(generator=...) points at")
                .isEqualTo("widget_seq_gen");
        assertThat(descriptor.sequenceName())
                .as("sequenceName is the physical db-side identifier used in SELECT nextval — must survive the mapping intact so the two concepts never get mixed up")
                .isEqualTo("widget_seq");
    }
}
