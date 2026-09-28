package cz.tstrecha.foundry.orm.entity;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.List;

@RequiredArgsConstructor
public class EntityParser {

    private final EntityContext entityContext;

    @SneakyThrows
    public <T> T parse(Class<T> clazz, List<String> columnLabels, List<String> row, EntityDefinition<T> definitions) {
        var entityCreationStrategy = entityContext.getEntityCreationStrategy(clazz);
        var entity = entityCreationStrategy.noArgConstructor().newInstance();

        for(int i = 0; i < columnLabels.size(); i++) {
            var label = columnLabels.get(i);
            var value = row.get(i);

            var columnDefinition = definitions.columns().get(label);
            var setter = entityCreationStrategy.parameterStrategies().get(columnDefinition.field()).setter();
            setter.invoke(entity, value);
        }

        return entity;
    }
}
