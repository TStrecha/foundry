package cz.tstrecha.foundry.orm.fixtures.registryscan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Declares its @SequenceGenerator next to the @Id field — the second legitimate
 * JPA placement. DescriptorRegistry must find it there as well.
 */
@Data
@Entity(name = "field_level")
@NoArgsConstructor
public class FieldLevelSequenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "field_level_seq")
    @SequenceGenerator(name = "field_level_seq", sequenceName = "field_level_seqdb", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "label")
    private String label;
}
