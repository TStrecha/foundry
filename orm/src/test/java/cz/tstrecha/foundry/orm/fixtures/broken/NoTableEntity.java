package cz.tstrecha.foundry.orm.fixtures.broken;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

/**
 * Intentionally missing @Entity annotation to exercise error paths.
 */
public class NoTableEntity {

    @Id
    @Column(name = "id")
    private Long id;
}
