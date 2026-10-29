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
public class TitlesFixture {

    @Column(name = "title_before")
    private String titleBefore;

    @Column(name = "title_after")
    private String titleAfter;
}
