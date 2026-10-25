package cz.tstrecha.foundry.orm.entity.registry;

import cz.tstrecha.foundry.orm.fixtures.broken.AbstractEntity;
import cz.tstrecha.foundry.orm.fixtures.broken.NoDefaultConstructorEntity;
import cz.tstrecha.foundry.orm.fixtures.broken.PrivateConstructorEntity;
import cz.tstrecha.foundry.orm.fixtures.good.BookEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManagedTypeCreatorTest {

    @Test
    void createsFreshInstanceOfTypeWithNoArgsConstructor() throws Exception {
        var creator = new ManagedTypeCreator<>(BookEntity.class);
        var book = creator.createNewEmptyInstance();
        assertThat(book).isInstanceOf(BookEntity.class);
        assertThat(book.getId()).isNull();
    }

    @Test
    void failsFastWhenNoArgsConstructorIsMissing() {
        assertThatThrownBy(() -> new ManagedTypeCreator<>(NoDefaultConstructorEntity.class))
                .isInstanceOf(NoSuchMethodException.class)
                .hasMessageContaining("0 arguments");
    }

    @Test
    void returnsNewInstanceOnEachCall() throws Exception {
        var creator = new ManagedTypeCreator<>(BookEntity.class);
        assertThat(creator.createNewEmptyInstance())
                .isNotSameAs(creator.createNewEmptyInstance());
    }

    @Test
    void supportsEntityWithPrivateNoArgsConstructor() throws Exception {
        var creator = new ManagedTypeCreator<>(PrivateConstructorEntity.class);
        assertThat(creator.createNewEmptyInstance()).isNotNull();
    }

    @Test
    void abstractClassIsRejectedWithClearError() {
        assertThatThrownBy(() -> new ManagedTypeCreator<>(AbstractEntity.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullTypeIsRejected() {
        assertThatThrownBy(() -> new ManagedTypeCreator<>(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
