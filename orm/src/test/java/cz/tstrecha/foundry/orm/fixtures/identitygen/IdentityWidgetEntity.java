package cz.tstrecha.foundry.orm.fixtures.identitygen;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal entity for the IDENTITY generator contract: a Long @Id with
 * {@code @GeneratedValue(strategy = IDENTITY)}. The value is produced by the
 * database during INSERT (not before), so the persist pipeline must round-trip
 * the generated id back onto the entity instance.
 */
@Data
@Entity(name = "identity_widget")
@NoArgsConstructor
@AllArgsConstructor
public class IdentityWidgetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "label")
    private String label;
}
