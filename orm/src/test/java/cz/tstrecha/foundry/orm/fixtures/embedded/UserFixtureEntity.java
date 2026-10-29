package cz.tstrecha.foundry.orm.fixtures.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "user_account")
@NoArgsConstructor
@AllArgsConstructor
public class UserFixtureEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "company_name")
    private String companyName;

    @Embedded
    private NameFixture name;
}
