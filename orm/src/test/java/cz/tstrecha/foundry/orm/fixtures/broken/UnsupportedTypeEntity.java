package cz.tstrecha.foundry.orm.fixtures.broken;

import cz.tstrecha.foundry.orm.operation.sql.Sql;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity(name = "unsupported")
public class UnsupportedTypeEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "born")
    private Sql born;
}
