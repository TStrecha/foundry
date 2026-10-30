package cz.tstrecha.foundry.orm.fixtures.registryscan;

/**
 * Scanning marker for fixtures used by DescriptorRegistry sequence-scan tests.
 * Isolated so the behaviours under test are not polluted by neighbouring
 * fixtures that happen to declare their own @SequenceGenerator.
 */
public final class RegistryScanMarker {
    private RegistryScanMarker() {
    }
}
