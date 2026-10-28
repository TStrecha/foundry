package cz.tstrecha.foundry.orm.fixtures.broken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity without any @Id annotation.
 */
@Data
@NoArgsConstructor
@Entity(name = "no_id")
public class NoIdEntity {

    @Column(name = "name")
    private String name;
}
