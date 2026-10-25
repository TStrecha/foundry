package cz.tstrecha.foundry.orm.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReflectionUtilsTest {

    static class Grandparent {
        private String grandparentField;
        static int grandparentStatic;
    }

    static class Parent extends Grandparent {
        private int parentField;
        protected String parentProtected;
    }

    static class Child extends Parent {
        private Long childField;
    }

    static class Standalone {
        private String a;
        public int b;
    }

    static class Empty {
    }

    @Test
    void findAllFieldsInClass_returnsDeclaredFieldsOfSimpleClass() {
        var fields = ReflectionUtils.findAllFieldsInClass(Standalone.class);
        assertThat(fields).extracting(Field::getName).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void findAllFieldsInClass_returnsEmptyForClassWithNoFields() {
        var fields = ReflectionUtils.findAllFieldsInClass(Empty.class);
        assertThat(fields).isEmpty();
    }

    @Test
    void findAllFieldsInClass_includesInheritedFieldsFromParent() {
        var fields = ReflectionUtils.findAllFieldsInClass(Parent.class);
        assertThat(fields).extracting(Field::getName)
                .contains("parentField", "parentProtected", "grandparentField");
    }

    @Test
    void findAllFieldsInClass_walksEntireClassHierarchy() {
        var fields = ReflectionUtils.findAllFieldsInClass(Child.class);
        assertThat(fields).extracting(Field::getName)
                .contains("childField", "parentField", "parentProtected", "grandparentField");
    }

    @Test
    void findAllFieldsInClass_includesPrivateFields() {
        var fields = ReflectionUtils.findAllFieldsInClass(Standalone.class);
        assertThat(fields).extracting(Field::getName).contains("a");
    }

    @Test
    void findAllFieldsInClass_includesStaticFields() {
        var fields = ReflectionUtils.findAllFieldsInClass(Grandparent.class);
        assertThat(fields).extracting(Field::getName).contains("grandparentStatic");
    }

    @Test
    void findAllFieldsInClass_doesNotDuplicateFieldsFromObject() {
        var fields = ReflectionUtils.findAllFieldsInClass(Standalone.class);
        assertThat(fields).extracting(Field::getName).containsOnly("a", "b");
    }

    @Test
    void findAllFieldsInClass_returnsChildFieldsBeforeParentFields() {
        var fields = ReflectionUtils.findAllFieldsInClass(Child.class);
        var names = fields.stream().map(Field::getName).toList();
        assertThat(names.indexOf("childField"))
                .as("fields from the subclass come before fields from its parents")
                .isLessThan(names.indexOf("parentField"));
    }

    @Test
    void findAllFieldsInClass_handlesInterface() {
        var fields = ReflectionUtils.findAllFieldsInClass(Runnable.class);
        assertThat(fields).isEmpty();
    }

    @Test
    void findAllFieldsInClass_rejectsNullType() {
        assertThatThrownBy(() -> ReflectionUtils.findAllFieldsInClass(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
