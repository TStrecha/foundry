package cz.tstrecha.foundry.orm.fixtures.uuidgen;

/**
 * Scanning marker for the UUID id-generator fixtures. Isolated into its own
 * package so a Foundry context aimed at it does not accidentally pull in
 * sequence-based fixtures that would collide with the uuid-only schema.
 */
public final class UuidGenMarker {
    private UuidGenMarker() {
    }
}
