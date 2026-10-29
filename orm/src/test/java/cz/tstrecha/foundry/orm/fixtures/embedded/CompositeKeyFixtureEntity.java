package cz.tstrecha.foundry.orm.fixtures.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "composite_entity")
@NoArgsConstructor
@AllArgsConstructor
public class CompositeKeyFixtureEntity {

    @EmbeddedId
    private CompositeKeyFixture pk;

    @Column(name = "payload")
    private String payload;
}
