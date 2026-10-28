package cz.tstrecha.foundry.orm.fixtures.broken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity(name = "id_without_column")
public class IdWithoutColumnEntity {

    @Id
    private Long id;

    @Column(name = "name")
    private String name;
}
