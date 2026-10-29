package cz.tstrecha.foundry.orm.fixtures;

import cz.tstrecha.foundry.orm.entity.descriptor.DescriptorRegistry;
import cz.tstrecha.foundry.orm.entity.descriptor.code.EntityDescriptor;

import java.util.List;

/**
 * Test-side helper: builds an {@link EntityDescriptor} for a given entity class
 * by spinning up a one-off {@link DescriptorRegistry} scoped to that class's package.
 *
 * Used to shim the old {@code new ManagedType<>(Class)} call sites after
 * ManagedType was changed to take an EntityDescriptor instead.
 */
public final class TestDescriptors {

    private TestDescriptors() {
    }

    @SuppressWarnings("unchecked")
    public static <T> EntityDescriptor<T> descriptorFor(Class<T> entityType) {
        var registry = new DescriptorRegistry(List.of(entityType));
        return (EntityDescriptor<T>) registry.getTypes().get(entityType);
    }
}
