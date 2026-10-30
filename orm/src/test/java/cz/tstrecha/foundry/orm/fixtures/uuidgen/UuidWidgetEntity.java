package cz.tstrecha.foundry.orm.fixtures.uuidgen;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Minimal entity for the UUID generator contract: a UUID @Id with
 * {@code @GeneratedValue(strategy = UUID)}. No @SequenceGenerator is involved —
 * JPA 3.1 UUID strategy generates the value in Java with no db roundtrip.
 */
@Data
@Entity(name = "uuid_widget")
@NoArgsConstructor
@AllArgsConstructor
public class UuidWidgetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "label")
    private String label;
}
