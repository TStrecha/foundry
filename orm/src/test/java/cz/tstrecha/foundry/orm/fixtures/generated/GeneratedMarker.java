package cz.tstrecha.foundry.orm.fixtures.generated;

/**
 * Scanning marker for fixtures used by the id-generator tests. Isolates these
 * entities from fixtures/good so a Foundry context aimed at this package does
 * not pick up BookEntity (which uses assigned ids and would conflict).
 */
public final class GeneratedMarker {
    private GeneratedMarker() {
    }
}
