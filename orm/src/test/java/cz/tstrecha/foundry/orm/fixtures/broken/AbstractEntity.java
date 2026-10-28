package cz.tstrecha.foundry.orm.fixtures.broken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "abstract_entity")
public abstract class AbstractEntity {

    @Id
    @Column(name = "id")
    private Long id;

    protected AbstractEntity() {
    }
}
