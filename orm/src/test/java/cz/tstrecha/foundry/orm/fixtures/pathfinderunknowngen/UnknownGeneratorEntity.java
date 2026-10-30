package cz.tstrecha.foundry.orm.fixtures.pathfinderunknowngen;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * References a generator name that no @SequenceGenerator in this package defines.
 * Honest-test subject for the "unknown generator" contract: resolving column paths
 * on this entity must fail fast at descriptor build time.
 */
@Data
@Entity(name = "unknown_gen")
@NoArgsConstructor
public class UnknownGeneratorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nothing_declares_me")
    @Column(name = "id")
    private Long id;

    @Column(name = "label")
    private String label;
}
