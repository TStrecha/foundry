package cz.tstrecha.foundry.orm.fixtures.broken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity(name = "unsupported")
public class UnsupportedTypeEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "born")
    private LocalDate born;
}
