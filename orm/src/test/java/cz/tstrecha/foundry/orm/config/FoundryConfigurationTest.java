package cz.tstrecha.foundry.orm.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FoundryConfigurationTest {

    @Test
    void acceptsAllRequiredFields() {
        var config = FoundryConfiguration.builder()
                .url("jdbc:postgresql://localhost:5432/db")
                .user("u")
                .password("p")
                .scanningRoots(List.of(FoundryConfigurationTest.class))
                .build();
        assertThat(config.url()).isEqualTo("jdbc:postgresql://localhost:5432/db");
        assertThat(config.user()).isEqualTo("u");
        assertThat(config.password()).isEqualTo("p");
        assertThat(config.scanningRoots()).containsExactly(FoundryConfigurationTest.class);
    }

    @Test
    void toStringNeverLeaksPassword() {
        var config = FoundryConfiguration.builder()
                .url("x").user("u").password("hunter2")
                .scanningRoots(List.of()).build();
        assertThat(config.toString()).doesNotContain("hunter2");
    }

    @Test
    void nullUrlIsRejected() {
        assertThatThrownBy(() -> FoundryConfiguration.builder()
                .url(null).user("u").password("p").scanningRoots(List.of()).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullUserIsRejected() {
        assertThatThrownBy(() -> FoundryConfiguration.builder()
                .url("x").user(null).password("p").scanningRoots(List.of()).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullPasswordIsRejected() {
        assertThatThrownBy(() -> FoundryConfiguration.builder()
                .url("x").user("u").password(null).scanningRoots(List.of()).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullScanningRootsIsRejected() {
        assertThatThrownBy(() -> FoundryConfiguration.builder()
                .url("x").user("u").password("p").scanningRoots(null).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emptyUrlIsRejected() {
        assertThatThrownBy(() -> FoundryConfiguration.builder()
                .url("").user("u").password("p").scanningRoots(List.of()).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankUserIsRejected() {
        assertThatThrownBy(() -> FoundryConfiguration.builder()
                .url("x").user("   ").password("p").scanningRoots(List.of()).build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
