import cz.tstrecha.foundry.orm.FoundryContext;
import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.entity.system.ColumnsEntity;
import cz.tstrecha.foundry.rttest.entity.Entities;
import cz.tstrecha.foundry.rttest.entity.UserEntity;

private final String URL = "jdbc:postgresql://localhost:5432/portify";
private final String USER = "portify";
private final String PASSWORD = "portify";

void main() {
    var config = FoundryConfiguration.builder()
            .url(URL)
            .user(USER)
            .password(PASSWORD)
            .scanningRoots(List.of(Entities.class))
            .build();
    var context = new FoundryContext(config);

    try(var entityManager = context.openSession()) {
        var columnsTable = entityManager.findAll(ColumnsEntity.class);
        var appUser = entityManager.find(UserEntity.class, 2L);

        columnsTable.forEach(entity -> {
            System.out.println(entity.getColumnName());
        });
        System.out.println(appUser.getId() + ": " + appUser.getAccountType() + " - " + appUser.getCompanyName());
    }
}