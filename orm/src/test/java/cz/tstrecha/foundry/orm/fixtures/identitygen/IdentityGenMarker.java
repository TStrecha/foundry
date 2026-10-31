package cz.tstrecha.foundry.orm.fixtures.identitygen;

/**
 * Scanning marker for the IDENTITY id-generator fixtures. Isolated into its own
 * package so a Foundry context aimed at it does not pull in sequence-based or
 * uuid-based fixtures that would collide with the identity-only schema.
 */
public final class IdentityGenMarker {
    private IdentityGenMarker() {
    }
}
