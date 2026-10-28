package cz.tstrecha.foundry.orm.fixtures.broken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "private_ctor")
public class PrivateConstructorEntity {

    @Id
    @Column(name = "id")
    private Long id;

    private PrivateConstructorEntity() {
    }

    public Long getId() {
        return id;
    }
}
