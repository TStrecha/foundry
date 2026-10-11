package cz.tstrecha.foundry.orm.fixtures.broken;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;
import cz.tstrecha.foundry.orm.definition.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Table("unsupported")
public class UnsupportedTypeEntity {

    @Id
    @Column("id")
    private Long id;

    @Column("born")
    private LocalDate born;
}
