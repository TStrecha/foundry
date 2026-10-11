package cz.tstrecha.foundry.orm.fixtures.broken;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;
import cz.tstrecha.foundry.orm.definition.Table;

@Table("private_ctor")
public class PrivateConstructorEntity {

    @Id
    @Column("id")
    private Long id;

    private PrivateConstructorEntity() {
    }

    public Long getId() {
        return id;
    }
}
