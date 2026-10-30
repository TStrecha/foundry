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
 * Declares its @SequenceGenerator at the class level — a legitimate JPA placement.
 * DescriptorRegistry must pick it up during scanning regardless of where the
 * annotation sits.
 */
@Data
@Entity(name = "class_level")
@NoArgsConstructor
@SequenceGenerator(name = "class_level_seq", sequenceName = "class_level_seqdb", allocationSize = 1)
public class ClassLevelSequenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "class_level_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "label")
    private String label;
}
