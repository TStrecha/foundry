package cz.tstrecha.foundry.rttest.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity(name = "app_user")
public class UserEntity {

    private static final String SEQUENCE_NAME = "user_seq";

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Long id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "account_type")
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
}
