package cz.tstrecha.foundry.orm.entity;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class EntityParser {

    private final EntityContext entityContext;

    @SneakyThrows
    public <T> T parse(Class<T> clazz, Set<String> columnLabels, List<String> row, EntityDefinition<T> definitions) {
        var labelsList = new ArrayList<>(columnLabels);
        var entityCreationStrategy = entityContext.getEntityCreationStrategy(clazz);
        var entity = entityCreationStrategy.noArgConstructor().newInstance();

        for(int i = 0; i < columnLabels.size(); i++) {
            var label = labelsList.get(i);
            var value = row.get(i);

            var columnDefinition = definitions.columns().get(label);
            var setter = entityCreationStrategy.parameterStrategies().get(columnDefinition.field()).setter();
            setter.invoke(entity, value);
        }

        return entity;
    }
}
