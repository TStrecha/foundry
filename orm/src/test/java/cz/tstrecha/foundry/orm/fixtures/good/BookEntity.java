package cz.tstrecha.foundry.orm.fixtures.good;

import cz.tstrecha.foundry.orm.fixtures.Genre;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity(name = "book")
public class BookEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "author")
    private String author;

    @Column(name = "genre")
    @Enumerated(EnumType.STRING)
    private Genre genre;

    @Column(name = "page_count")
    private Long pageCount;
}
