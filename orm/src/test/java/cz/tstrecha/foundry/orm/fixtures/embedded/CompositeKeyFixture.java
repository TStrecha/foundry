package cz.tstrecha.foundry.orm.fixtures.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompositeKeyFixture {

    @Column(name = "part_a")
    private String partA;

    @Column(name = "part_b")
    private String partB;
}
