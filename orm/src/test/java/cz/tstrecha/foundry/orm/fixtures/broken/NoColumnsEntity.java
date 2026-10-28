package cz.tstrecha.foundry.orm.fixtures.broken;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity(name = "no_columns")
public class NoColumnsEntity {

    private String randomField;
}
