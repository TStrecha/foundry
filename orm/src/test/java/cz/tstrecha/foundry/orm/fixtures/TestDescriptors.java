package cz.tstrecha.foundry.orm.fixtures;

import cz.tstrecha.foundry.orm.entity.descriptor.DescriptorRegistry;
import cz.tstrecha.foundry.orm.entity.descriptor.code.EntityDescriptor;

import java.util.List;

/**
 * Test-side helper for building an {@link EntityDescriptor} without pulling in
 * every neighbouring class in the fixture package.
 */
public final class TestDescriptors {

    private TestDescriptors() {
    }

    /**
     * Scans the whole package of {@code entityType}. Useful for "happy path" tests
     * where every fixture in the package is well-formed. Do NOT use for tests
     * targeting a specific broken fixture — a different broken fixture in the same
     * package can cause the scan to fail first, hiding the real behaviour under test.
     */
    @SuppressWarnings("unchecked")
    public static <T> EntityDescriptor<T> descriptorFor(Class<T> entityType) {
        var registry = new DescriptorRegistry(List.of(entityType));
        return (EntityDescriptor<T>) registry.getTypes().get(entityType);
    }

    /**
     * Builds the EntityDescriptor for exactly this one class without scanning its
     * package. Any @Embeddable types it references are resolved lazily through a
     * minimal registry on demand. Prefer this for tests targeting a specific entity
     * so that neighbouring broken fixtures cannot pollute the result.
     */
    public static <T> EntityDescriptor<T> isolatedDescriptorFor(Class<T> entityType) {
        var registry = new DescriptorRegistry(List.of());
        return new EntityDescriptor<>(entityType, registry);
    }
}
