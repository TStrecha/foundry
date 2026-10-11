package cz.tstrecha.foundry.orm.fixtures.broken;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;
import cz.tstrecha.foundry.orm.definition.Table;

@Table("abstract_entity")
public abstract class AbstractEntity {

    @Id
    @Column("id")
    private Long id;

    protected AbstractEntity() {
    }
}
