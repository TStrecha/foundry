package cz.tstrecha.foundry.orm.entity.descriptor;

import cz.tstrecha.foundry.orm.fixtures.embedded.NameFixture;
import cz.tstrecha.foundry.orm.fixtures.embedded.UserFixtureEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldPathTest {

    @Test
    void emptyFieldPathHasNoNodes() {
        assertThat(new FieldPath().nodes()).isEmpty();
    }

    @Test
    void appendNewReturnsPathWithOneAdditionalNode() throws Exception {
        var base = new FieldPath();
        var extended = base.appendNew(UserFixtureEntity.class.getDeclaredField("name"));

        assertThat(extended.nodes()).hasSize(1);
        assertThat(extended.nodes().get(0).field().getName()).isEqualTo("name");
    }

    @Test
    void appendNewDoesNotMutateOriginalPath() throws Exception {
        var base = new FieldPath();
        base.appendNew(UserFixtureEntity.class.getDeclaredField("name"));

        assertThat(base.nodes())
                .as("appendNew must leave the original FieldPath untouched — this used to leak shared ArrayList state across all paths")
                .isEmpty();
    }

    @Test
    void twoAppendNewsFromSameBaseProduceIndependentPaths() throws Exception {
        var base = new FieldPath();
        var withName = base.appendNew(UserFixtureEntity.class.getDeclaredField("name"));
        var withCompanyName = base.appendNew(UserFixtureEntity.class.getDeclaredField("companyName"));

        assertThat(withName.nodes()).hasSize(1);
        assertThat(withCompanyName.nodes()).hasSize(1);
        assertThat(withName.nodes().get(0).field().getName()).isEqualTo("name");
        assertThat(withCompanyName.nodes().get(0).field().getName()).isEqualTo("companyName");
    }

    @Test
    void chainedAppendNewsBuildCumulativePath() throws Exception {
        var base = new FieldPath();
        var level1 = base.appendNew(UserFixtureEntity.class.getDeclaredField("name"));
        var level2 = level1.appendNew(NameFixture.class.getDeclaredField("titles"));

        assertThat(level2.nodes())
                .extracting(n -> n.field().getName())
                .containsExactly("name", "titles");
    }

    @Test
    void pathNodeInstantiateCreatesFreshInstance() throws Exception {
        var fieldPath = new FieldPath().appendNew(UserFixtureEntity.class.getDeclaredField("name"));
        var instance = fieldPath.nodes().get(0).instantiate();

        assertThat(instance).isInstanceOf(NameFixture.class);
    }

    @Test
    void pathNodeTypeMatchesFieldType() throws Exception {
        var fieldPath = new FieldPath().appendNew(UserFixtureEntity.class.getDeclaredField("name"));

        assertThat(fieldPath.nodes().get(0).type()).isEqualTo(NameFixture.class);
    }

    @Test
    void appendNewWithNullFieldIsRejected() {
        var base = new FieldPath();

        assertThatThrownBy(() -> base.appendNew(null))
                .as("appending a null Field is a programmer error and must be rejected explicitly")
                .isInstanceOf(IllegalArgumentException.class);
    }
}
