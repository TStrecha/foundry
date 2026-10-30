package cz.tstrecha.foundry.orm.fixtures.generated;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "widget")
@NoArgsConstructor
@AllArgsConstructor
public class SequencedWidgetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "widget_seq_gen")
    @SequenceGenerator(name = "widget_seq_gen", sequenceName = "widget_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "label")
    private String label;
}
