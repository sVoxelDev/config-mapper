package net.silthus.configmapper;

import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SuppressWarnings("ALL")
public class ConfigMapTests {

    @Nested
    @DisplayName("apply ConfigMapt to object")
    class ConfigMapApplier {

        private ConfigMap configMap;

        @SneakyThrows
        @BeforeEach
        void setup() {

            configMap = ConfigMap.of(ConfiguredObject.class)
                    .with(
                            KeyValuePair.of("test.required", 2),
                            KeyValuePair.of("test.default_field", "foobar"),
                            KeyValuePair.of("test.all_annotations", "5")
                    );
        }

        @Test
        @DisplayName("should combine multiple key value pair with(...) calls")
        void shouldCombineMultipleKeyValuePairs() throws ConfigurationException {

            ConfigMap configMap = this.configMap.with(
                    KeyValuePair.of("test.required", 2),
                    KeyValuePair.of("test.default_field", "foobar"),
                    KeyValuePair.of("test.all_annotations", "5")
            ).with(
                    KeyValuePair.of("val1", "foobar")
            );

            assertThat(configMap.keyValuePairs().size())
                    .isEqualTo(4);
        }

        @SneakyThrows
        @Test
        @DisplayName("should set all fields in the target object")
        void shouldSetAllFieldsCorrectly() {

            ConfiguredObject object = configMap
                    .with(
                            KeyValuePair.of("val1", "bar"),
                            KeyValuePair.of("val2", true)
                    ).applyTo(new ConfiguredObject());

            assertThat(object)
                .extracting(
                        ConfiguredObject::getVal1,
                        ConfiguredObject::isVal2
                ).contains(
                    "bar",
                    true
                );
        }

        @SneakyThrows
        @Test
        @DisplayName("should map nested config object")
        void shouldMapNestedConfigObject() {

            ConfiguredObject config = configMap.applyTo(new ConfiguredObject());

            assertThat(config).extracting(ConfiguredObject::getTest)
                    .extracting(
                            TestConfig::getRequired,
                            TestConfig::getDefaultField,
                            TestConfig::getAllAnnotations
                    )
                    .contains(2, "foobar", 5.0d);
        }

        @SneakyThrows
        @Test
        @DisplayName("should ignore unmapped fields")
        void shouldIgnoreUnmappedProperties() {

            ConfiguredObject object = configMap
                    .with(
                            KeyValuePair.of("ignored", "2.0")
                    ).applyTo(new ConfiguredObject());

            assertThat(object)
                    .extracting(
                            ConfiguredObject::getIgnored
                    ).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("load ConfigMap from class")
    class ConfigMapLoader {

        @Test
        @DisplayName("should load all fields including superclass")
        public void shouldLoadAllFields() {

            assertThatCode(() -> assertThat(ConfigUtil.getConfigFields(TestConfig.class))
                    .hasSizeGreaterThanOrEqualTo(4)
                    .containsKeys(
                            "parent_field",
                            "required",
                            "default_field",
                            "all_annotations"
                    )
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should load deeply nested fields including superclass")
        void shouldLoadDeeplyNestedFields() {

            assertThatCode(() -> assertThat(ConfigUtil.getConfigFields(SubClassWithParentFields.class))
                    .containsKeys(
                            "sub.duration",
                            "sub.annotated.foo"
                    )
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should load required annotation")
        public void shouldLoadRequiredAttribute() {

            assertThatCode(() -> assertThat(ConfigUtil.getConfigFields(TestConfig.class))
                    .extractingByKey("required")
                    .extracting(ConfigFieldInformation::required)
                    .isEqualTo(true)
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should load description annotation")
        public void shouldLoadDescriptionAttribute() {

            assertThatCode(() -> assertThat(ConfigUtil.getConfigFields(TestConfig.class))
                    .extractingByKey("default_field")
                    .extracting(ConfigFieldInformation::description)
                    .isEqualTo(new String[]{"World to teleport the player to."})
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should load default value")
        public void shouldLoadDefaultValue() {

            assertThatCode(() -> assertThat(ConfigUtil.getConfigFields(TestConfig.class))
                    .extractingByKey("default_field")
                    .extracting(ConfigFieldInformation::defaultValue)
                    .isEqualTo("world")
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should load required field with default value")
        public void shouldLoadRequiredDefaultValue() {

            assertThatCode(() -> assertThat(ConfigUtil.getConfigFields(TestConfig.class))
                    .extractingByKey("all_annotations")
                    .extracting(ConfigFieldInformation::defaultValue, ConfigFieldInformation::description)
                    .contains(2.0d, new String[]{"Required field with default value."})
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should load nested config objects")
        public void shouldLoadNestedObjects() {

            assertThatCode(() -> assertThat(ConfigUtil.getConfigFields(TestConfig.class))
                    .extractingByKey("nested.nested_field")
                    .extracting(ConfigFieldInformation::description, ConfigFieldInformation::defaultValue)
                    .contains(new String[]{"nested config field"}, "foobar")
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should not load nested object fields")
        public void shouldNotAddNestedBase() {
            assertThatCode(() -> assertThat(ConfigUtil.getConfigFields(TestConfig.class))
                    .doesNotContainKey("nested")
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should ignore fields without an annotation")
        public void shouldIgnoredIgnored() {
            assertThatCode(() -> assertThat(ConfigUtil.getConfigFields(TestConfig.class))
                    .doesNotContainKeys("ignored", "no_annotations")
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should load field position annotation")
        public void shouldLoadFieldPosition() {

            assertThatCode(() -> assertThat(ConfigUtil.getConfigFields(TestConfig.class))
                    .extractingByKeys("required", "parent_field")
                    .extracting(ConfigFieldInformation::position)
                    .contains(1, 0)
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should throw if same field position is found")
        public void shouldThrowExceptionForSamePosition() {

            assertThatExceptionOfType(ConfigurationException.class)
                    .isThrownBy(() -> ConfigUtil.getConfigFields(SamePositionConfig.class))
                    .withMessageContaining("same position");
        }

        @Test
        @DisplayName("should throw if declared config field is final")
        void shouldThrowIfConfigOptionIsFinal() {

            assertThatExceptionOfType(ConfigurationException.class)
                    .isThrownBy(() -> ConfigUtil.getConfigFields(FinalConfig.class))
                    .withMessageContaining("final field");
        }

        @Test
        @DisplayName("should load all fields if the class is annotated")
        void shouldLoadAllFieldsInAnnotatedClass() {

            assertThatCode(() -> assertThat(ConfigUtil.getConfigFields(AnnotatedClass.class))
                    .containsOnlyKeys("foo", "bar")
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should load array field")
        void shouldLoadArrayField() {

            assertThatCode(() -> assertThat(ConfigUtil.getConfigFields(ArrayConfig.class))
                    .containsOnlyKeys("foo", "array")
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should load and set properties in abstract base class")
        void shouldLoadPropertyFromBaseClass() {

            SubClass subClass = ConfigMap.of(SubClass.class)
                    .with(KeyValuePair.of("duration", 30))
                    .applyTo(new SubClass());

            assertThat(subClass.getDuration())
                    .isEqualTo(30);
        }

        @Test
        @DisplayName("should load and set properties from deeply nested sub classes")
        void shouldLoadPropertyFromDeeplyNestedSubClass() {

            SubClassWithParentFields cfg = ConfigMap.of(SubClassWithParentFields.class)
                    .with(
                            KeyValuePair.of("sub.duration", 30),
                            KeyValuePair.of("sub.annotated.foo", 100)
                    )
                    .applyTo(new SubClassWithParentFields());

            assertThat(cfg)
                    .extracting(config -> config.sub.getDuration(), config -> config.sub.annotated.foo)
                    .contains(30, 100);
        }
    }

    public static class SamePositionConfig {

        @ConfigOption(position = 1)
        private int pos1;
        @ConfigOption(position = 1)
        private int pos2;
    }


    public static class FinalConfig {

        @ConfigOption
        private final int myFinalField = 20;
    }

    public static class FinalConfigDefaultIgnore {

        private String foo;

        private final String bar = "is ignored";
    }

    public static class ConfigBase {

        @ConfigOption(position = 0)
        private String parentField = "foobar";
    }

    @Data
    public static class TestConfig extends ConfigBase {

        private boolean noAnnotations;
        @ConfigOption(required = true, position = 1)
        private int required;
        @ConfigOption(description = "World to teleport the player to.")
        private String defaultField = "world";

        @ConfigOption(description = "Required field with default value.", required = true)
        private double allAnnotations = 2.0d;

        private String ignored = "";

        @ConfigOption
        private NestedConfig nested = new NestedConfig();

        private final String myFinalField = "is ignored";
    }

    public static class NestedConfig {
        @ConfigOption(description = "nested config field")
        private String nestedField = "foobar";
    }

    public static class ErrorConfig extends ConfigBase {

        @ConfigOption(position = 0)
        private int error = 2;
    }

    @ConfigOption
    public static class AnnotatedClass {

        private int foo;
        private String bar;
        @Ignore
        private double ignored;
    }

    @ConfigOption
    public static class ArrayConfig {

        private int foo;
        private String[] array;
    }

    @Data
    public static class ConfiguredObject {

        @ConfigOption
        private String val1 = "foo";

        @ConfigOption
        private boolean val2;

        @ConfigOption
        private TestConfig test = new TestConfig();

        private double ignored = 1.0;
    }

    @Data
    public static abstract class AbstractBaseClass {

        @ConfigOption
        private int duration = 10;
    }

    public static class SubClass extends AbstractBaseClass {

        @ConfigOption
        private AnnotatedClass annotated = new AnnotatedClass();
    }

    public static class SubClassWithParentFields {

        @ConfigOption
        private SubClass sub = new SubClass();
    }
}
