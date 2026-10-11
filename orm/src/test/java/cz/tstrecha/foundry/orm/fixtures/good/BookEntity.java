package cz.tstrecha.foundry.orm.fixtures.good;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;
import cz.tstrecha.foundry.orm.definition.Table;
import cz.tstrecha.foundry.orm.fixtures.Genre;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table("book")
public class BookEntity {

    @Id
    @Column("id")
    private Long id;

    @Column("title")
    private String title;

    @Column("author")
    private String author;

    @Column("genre")
    private Genre genre;

    @Column("page_count")
    private Long pageCount;
}
