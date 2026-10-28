package cz.tstrecha.foundry.orm.fixtures.broken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "no_ctor")
public class NoDefaultConstructorEntity {

    @Id
    @Column(name = "id")
    private Long id;

    public NoDefaultConstructorEntity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
