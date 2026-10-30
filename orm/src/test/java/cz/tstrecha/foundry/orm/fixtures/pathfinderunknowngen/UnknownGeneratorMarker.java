package cz.tstrecha.foundry.orm.fixtures.pathfinderunknowngen;

/**
 * Scanning marker for the "unknown generator" fixture. Isolated into its own
 * package so a context scanning for it does not pick up well-formed entities
 * from fixtures/good or fixtures/generated.
 */
public final class UnknownGeneratorMarker {
    private UnknownGeneratorMarker() {
    }
}
