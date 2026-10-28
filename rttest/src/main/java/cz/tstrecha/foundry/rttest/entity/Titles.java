package cz.tstrecha.foundry.rttest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Titles {

    @Column(name = "title_before")
    private String titleBefore;

    @Column(name = "title_after")
    private String titleAfter;

}
