package cz.tstrecha.foundry.orm.fixtures.broken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity(name = "multi_id")
public class MultipleIdsEntity {

    @Id
    @Column(name = "id1")
    private Long id1;

    @Id
    @Column(name = "id2")
    private Long id2;
}
