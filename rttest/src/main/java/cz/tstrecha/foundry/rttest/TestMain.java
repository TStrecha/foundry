import cz.tstrecha.foundry.orm.FoundryContext;
import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.entity.system.ColumnsEntity;
import cz.tstrecha.foundry.orm.entity.system.ColumnsEntityId;
import cz.tstrecha.foundry.rttest.entity.*;
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
        entityManager.runInTransaction(_ -> {
            var event = new EventLogEntity();
            event.setEventType("USER_LOGIN");
            event.setMessage("Testing IDENTITY generator");
            entityManager.persist(event);
            System.out.println("Generated id: " + event.getId());
            System.out.println("Generated occurrence: " + event.getOccurrenceNo());
            System.out.println("Generated audit: " + event.getAuditNo());

            var column = entityManager.find(ColumnsEntity.class,
                    new ColumnsEntityId("portify", "public", "app_user", "company_name"));
            System.out.println(column);

            var moneyTransactions = entityManager.findAll(MoneyTransactionEntity.class);

            moneyTransactions.forEach(entity -> {
                System.out.println(entity.getIdentifier());
            });

            var newUser = new UserEntity();
            newUser.setAccountType(AccountType.PERSON);
            newUser.setCompanyName("TestCompany123");
            newUser.setName(new UserName("Thomas", "Tester"));

            entityManager.persist(newUser);
            System.out.println("Generated id: " + newUser.getId());

            var appUser = entityManager.find(UserEntity.class, 2L).orElseThrow(() -> new NoSuchElementException("User not found"));
            System.out.println(appUser.getId() + ": " + appUser.getAccountType() + " - " + appUser.getCompanyName());

            appUser.setCompanyName("testing");
            appUser.setName(new UserName("1", null));

            var appUser2 = entityManager.find(UserEntity.class, 2L).orElseThrow(() -> new NoSuchElementException("User not found"));
            System.out.println(appUser2.getId() + ": " + appUser2.getAccountType() + " - " + appUser2.getCompanyName());
            System.out.println("App users are equal: " + (appUser == appUser2));
        });
    }
}