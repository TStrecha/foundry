import cz.tstrecha.foundry.orm.connection.FoundryContext;
import cz.tstrecha.foundry.orm.entity.system.ColumnsEntity;
import cz.tstrecha.foundry.rttest.entity.Entities;
import cz.tstrecha.foundry.rttest.entity.UserEntity;

private final String URL = "jdbc:postgresql://localhost:5432/portify";
private final String USERNAME = "portify";
private final String PASSWORD = "portify";

void main() {
    var context = FoundryContext.builder()
            .url(URL)
            .username(USERNAME)
            .password(PASSWORD)
            .entitySourceRoot(Entities.class)
            .build();

    var session = context.openSession();
    var entityManager = session.getEntityManager();
    var columnsTable = entityManager.findAll(ColumnsEntity.class);
    var appUser = entityManager.find(UserEntity.class, "2");

    columnsTable.forEach(entity -> {
        System.out.println(entity.getColumnName());
    });
    System.out.println(appUser.getCompanyName());

    session.close();
}