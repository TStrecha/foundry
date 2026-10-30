package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.entity.descriptor.code.EmbeddableTypeDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.code.EntityDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.code.FieldDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.code.TypeDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.db.SequenceDescriptor;
import cz.tstrecha.foundry.orm.entity.system.SystemEntities;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

@Data
public class DescriptorRegistry {

    private final Map<Class<?>, TypeDescriptor> types = new HashMap<>();
    private final Map<String, SequenceDescriptor> sequences = new HashMap<>();

    public DescriptorRegistry(List<Class<?>> scanningRoots) {
        requireNotNullArgument(scanningRoots, "scanningRoots");

        var packageNames = Stream.concat(scanningRoots.stream(), Stream.of(SystemEntities.class))
                .map(Class::getPackageName)
                .toArray(String[]::new);
        var filterBuilder = new FilterBuilder();
        for (var pkg : packageNames) {
            filterBuilder.includePackage(pkg);
        }
        var reflectionsConfig = new ConfigurationBuilder()
                .forPackages(packageNames)
                .filterInputsBy(filterBuilder);
        var reflections = new Reflections(reflectionsConfig);

        Stream.concat(
                        reflections.getTypesAnnotatedWith(Embeddable.class).stream(),
                        reflections.getTypesAnnotatedWith(Entity.class).stream())
                .forEach(this::computeTypeIfAbsent);

        this.types.forEach((type, descriptor) -> {
            Optional.ofNullable(type.getAnnotation(SequenceGenerator.class))
                    .ifPresent(this::computeSequenceIfAbsent);

            descriptor.fields().stream()
                    .map(FieldDescriptor::field)
                    .map(field -> field.getAnnotation(SequenceGenerator.class))
                    .filter(Objects::nonNull)
                    .forEach(this::computeSequenceIfAbsent);
        });
    }

    public TypeDescriptor computeTypeIfAbsent(Class<?> type) {
        return types.computeIfAbsent(type, this::createTypeDescriptor);
    }

    public void computeSequenceIfAbsent(SequenceGenerator sequenceGenerator) {
        sequences.computeIfAbsent(sequenceGenerator.name(), _ -> new SequenceDescriptor(sequenceGenerator));
    }

    private TypeDescriptor createTypeDescriptor(Class<?> type) {
        if(type.isAnnotationPresent(Embeddable.class)) {
            return new EmbeddableTypeDescriptor(type, this);
        }
        if(type.isAnnotationPresent(Entity.class)) {
            return new EntityDescriptor<>(type, this);
        }

        throw new IllegalArgumentException("Type " + type + " cannot be described");
    }
}
