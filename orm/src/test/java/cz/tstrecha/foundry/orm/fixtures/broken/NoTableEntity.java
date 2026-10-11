package cz.tstrecha.foundry.orm.fixtures.broken;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;

/**
 * Intentionally missing @Table annotation to exercise error paths.
 */
public class NoTableEntity {

    @Id
    @Column("id")
    private Long id;
}
