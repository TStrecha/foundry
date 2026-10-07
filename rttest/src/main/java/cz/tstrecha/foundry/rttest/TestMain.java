import cz.tstrecha.foundry.orm.FoundryContext;
import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.entity.system.ColumnsEntity;
import cz.tstrecha.foundry.rttest.entity.AccountType;
import cz.tstrecha.foundry.rttest.entity.Entities;
import cz.tstrecha.foundry.rttest.entity.MoneyTransactionEntity;
import cz.tstrecha.foundry.rttest.entity.UserEntity;
import lombok.SneakyThrows;

private final String URL = "jdbc:postgresql://localhost:5432/portify";
private final String USER = "portify";
private final String PASSWORD = "portify";

@SneakyThrows
void main() {
    var config = FoundryConfiguration.builder()
            .url(URL)
            .user(USER)
            .password(PASSWORD)
            .scanningRoots(List.of(Entities.class))
            .build();
    var context = new FoundryContext(config);

    try(var entityManager = context.openSession()) {
        entityManager.runInTransaction(() -> {

            var columnsTable = entityManager.findAll(MoneyTransactionEntity.class);

            columnsTable.forEach(entity -> {
                System.out.println(entity.getIdentifier());
            });

            var newUser = new UserEntity();
            newUser.setId(new Random().nextLong());
            newUser.setAccountType(AccountType.PERSON);
            newUser.setCompanyName("TestCompany123");
            newUser.setFirstName("Thomas");
            newUser.setLastName("Tester");

            entityManager.persist(newUser);

            var appUser = entityManager.find(UserEntity.class, 2L).orElseThrow(() -> new NoSuchElementException("User not found"));
            System.out.println(appUser.getId() + ": " + appUser.getAccountType() + " - " + appUser.getCompanyName());

            var appUser2 = entityManager.find(UserEntity.class, 2L).orElseThrow(() -> new NoSuchElementException("User not found"));
            System.out.println(appUser2.getId() + ": " + appUser2.getAccountType() + " - " + appUser2.getCompanyName());
        });
    }
}