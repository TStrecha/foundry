package cz.tstrecha.foundry.orm.fixtures.broken;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;
import cz.tstrecha.foundry.orm.definition.Table;

@Table("no_ctor")
public class NoDefaultConstructorEntity {

    @Id
    @Column("id")
    private Long id;

    public NoDefaultConstructorEntity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
