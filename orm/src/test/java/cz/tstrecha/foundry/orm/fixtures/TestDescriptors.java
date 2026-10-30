package cz.tstrecha.foundry.orm.fixtures;

import cz.tstrecha.foundry.orm.entity.descriptor.DescriptorRegistry;
import cz.tstrecha.foundry.orm.entity.descriptor.ManagedColumnDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.PathFinder;
import cz.tstrecha.foundry.orm.entity.descriptor.code.EntityDescriptor;
import cz.tstrecha.foundry.orm.entity.persister.ManagedType;

import java.util.List;

/**
 * Test-side helper for building an {@link EntityDescriptor} without pulling in
 * every neighbouring class in the fixture package.
 */
public final class TestDescriptors {

    private TestDescriptors() {
    }

    /**
     * Entity's descriptor paired with the registry it was built from. Needed by
     * tests that construct a ManagedType/ManagedColumn, which both now take a
     * DescriptorRegistry (to look up sequence/generator metadata).
     */
    public record DescriptorBundle<T>(EntityDescriptor<T> descriptor, DescriptorRegistry registry) {
    }

    /**
     * Scans the whole package of {@code entityType}. Useful for "happy path" tests
     * where every fixture in the package is well-formed. Do NOT use for tests
     * targeting a specific broken fixture — a different broken fixture in the same
     * package can cause the scan to fail first, hiding the real behaviour under test.
     */
    public static <T> EntityDescriptor<T> descriptorFor(Class<T> entityType) {
        return descriptorAndRegistryFor(entityType).descriptor();
    }

    @SuppressWarnings("unchecked")
    public static <T> DescriptorBundle<T> descriptorAndRegistryFor(Class<T> entityType) {
        var registry = new DescriptorRegistry(List.of(entityType));
        return new DescriptorBundle<>((EntityDescriptor<T>) registry.getTypes().get(entityType), registry);
    }

    /**
     * Builds the EntityDescriptor for exactly this one class without scanning its
     * package. Any @Embeddable types it references are resolved lazily through a
     * minimal registry on demand. Prefer this for tests targeting a specific entity
     * so that neighbouring broken fixtures cannot pollute the result.
     */
    public static <T> EntityDescriptor<T> isolatedDescriptorFor(Class<T> entityType) {
        return isolatedDescriptorAndRegistryFor(entityType).descriptor();
    }

    public static <T> DescriptorBundle<T> isolatedDescriptorAndRegistryFor(Class<T> entityType) {
        var registry = new DescriptorRegistry(List.of());
        return new DescriptorBundle<>(new EntityDescriptor<>(entityType, registry), registry);
    }

    /** Build a ManagedType with its own scanning registry — one-liner for happy-path tests. */
    public static <T> ManagedType<T> managedTypeFor(Class<T> entityType) throws NoSuchMethodException {
        var bundle = descriptorAndRegistryFor(entityType);
        return new ManagedType<>(bundle.descriptor(), bundle.registry());
    }

    /** Build a ManagedType in isolation — for tests targeting a specific broken fixture. */
    public static <T> ManagedType<T> isolatedManagedTypeFor(Class<T> entityType) throws NoSuchMethodException {
        var bundle = isolatedDescriptorAndRegistryFor(entityType);
        return new ManagedType<>(bundle.descriptor(), bundle.registry());
    }

    /**
     * Walks {@code entityType} with the registry it was scanned from and returns the entity-rooted
     * column paths. Convenience over {@link PathFinder#buildColumnPaths} so tests do not have to
     * carry both the descriptor and the registry.
     */
    public static List<ManagedColumnDescriptor<?>> columnPathsFor(Class<?> entityType) {
        var bundle = descriptorAndRegistryFor(entityType);
        return PathFinder.buildColumnPaths(bundle.descriptor(), bundle.registry());
    }

    /** Same as {@link #columnPathsFor} but for the id paths. */
    public static List<ManagedColumnDescriptor<?>> idPathsFor(Class<?> entityType) {
        var bundle = descriptorAndRegistryFor(entityType);
        return PathFinder.buildIdPaths(bundle.descriptor(), bundle.registry());
    }
}
