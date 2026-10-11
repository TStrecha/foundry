package cz.tstrecha.foundry.orm.fixtures.good;

/**
 * Marker interface used as a scanning root for PersisterRegistry during tests.
 * Lives in the `good` sub-package so Reflections won't pick up the intentionally
 * broken fixtures.
 */
public interface EntitiesMarker {
}
