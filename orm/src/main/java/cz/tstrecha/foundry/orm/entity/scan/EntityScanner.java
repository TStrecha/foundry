package cz.tstrecha.foundry.orm.entity.scan;

import cz.tstrecha.foundry.orm.definition.Table;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.util.Arrays;

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
