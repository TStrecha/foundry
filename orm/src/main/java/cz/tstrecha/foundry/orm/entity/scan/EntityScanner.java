package cz.tstrecha.foundry.orm.entity.scan;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;
import cz.tstrecha.foundry.orm.definition.Table;
import cz.tstrecha.foundry.orm.entity.ColumnDefinition;
import cz.tstrecha.foundry.orm.entity.EntityCreationStrategy;
import cz.tstrecha.foundry.orm.entity.EntityDefinition;
import cz.tstrecha.foundry.orm.entity.EntityParameterInitializationStrategy;
import cz.tstrecha.foundry.orm.entity.type.PostgresColumnTypeIdentifier;
import lombok.SneakyThrows;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityScanner {

    public static EntityScanResult scan(Class<?>... scanningRoots) {
        var packageNames = Arrays.stream(scanningRoots).map(Class::getPackageName).distinct().toArray(String[]::new);
        var reflectionsConfig = new ConfigurationBuilder().forPackages(packageNames);
        var reflections = new Reflections(reflectionsConfig);

        var entities = reflections.getTypesAnnotatedWith(Table.class);
        var entityDefinitions = EntityDefinitionScanner.scan(entities);
        var creationStrategies = EntityBuildingStrategyScanner.scan(entities);

        return new EntityScanResult(entities, entityDefinitions, creationStrategies);
    }

}
