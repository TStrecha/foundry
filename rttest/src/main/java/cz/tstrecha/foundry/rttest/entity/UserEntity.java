package cz.tstrecha.foundry.rttest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "app_user")
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    private static final String SEQUENCE_NAME = "user_seq";

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Long id;

    @Column(name = "company_name")
    private String companyName;

    @Embedded
    private UserName name;

    @Column(name = "account_type")
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
}
