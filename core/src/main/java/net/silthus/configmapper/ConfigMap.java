/*
 * Copyright 2020 ART-Framework Contributors (https://github.com/Silthus/art-framework)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.silthus.configmapper;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.NonFinal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The ConfigMap holds information about all fields and their type inside your config class.
 * <p>Use the {@link ConfigMap#of(Class)} method to create a new ConfigMap from your class.
 * <p>This will create a new instance of your class and search all fields annotated with @{@link ConfigOption}.
 * The fields are then stored as {@link ConfigFieldInformation} and mapped to their keys.
 * <p>Use the {@link #with(KeyValuePair...)} method to load the actual config values into the ConfigMap.
 * This is required before you can apply the config to your object.
 * <p>Then apply the config to an intance of your config class with {@link #applyTo(Object)}.
 * <p><pre>{@code
 * ConfigMap.of(MyConfig.class)
 *      .with(KeyValuePair.of("key", "value")
 *      .applyTo(new MyConfig());
 * }</pre>
 */
@Value
@NonFinal
@Accessors(fluent = true)
public class ConfigMap<TConfig> {

    /**
     * Tries to fetch all annotated config fields of the given class.
     * <p>The given class must have a parameterless public constructor.
     * Use the {@link #of(Class, Supplier)} method if you class has a different signature.
     * <p>Only fields annotated with @{@link ConfigOption} will be cataloged or sub classes that
     * have the {@code @ConfigOption} annotation.
     *
     * @param configClass the config class that should be analyzed for configured fiels
     * @return a ConfigMap of all fields inside the given class
     * @throws ConfigurationException if the class cannot be instantiated (e.g. no public constructor)
     *                                or if a mapping failed
     */
    public static <TConfig> ConfigMap<TConfig> of(Class<TConfig> configClass) {

        return of(configClass, ConfigUtil.getConfigFields(configClass));
    }

    /**
     * Creates a new ConfigMap using the given supplier to create the config object.
     * <p>This is the alternative to the {@link ConfigMap#of(Class)} method that does not require a
     * public parameterless constructor.
     *
     * @param configClass the config class that should be analyzed for configured fiels
     * @param supplier the supplier used to create a fresh instance of the config class
     * @param <TConfig> the type of the config
     * @return a ConfigMap of all fields inside the given class
     * @throws ConfigurationException if the config class contains invalid field mappings
     * @see ConfigMap#of(Class)
     */
    public static <TConfig> ConfigMap<TConfig> of(Class<TConfig> configClass, Supplier<TConfig> supplier) {

        return of(configClass, ConfigUtil.getConfigFields(configClass, supplier.get()));
    }

    /**
     * Creates a new ConfigMap using the provided config instance to map field default values.
     * <p>No new instance of the config will be created by this method.
     *
     * @param config the config object that should be scanned for config fields
     * @param <TConfig> the type of the config
     * @return the config map for the given config object
     */
    public static <TConfig> ConfigMap<TConfig> of(TConfig config) {

        return new ConfigMap<>(config, ConfigUtil.getConfigFields(config));
    }

    /**
     * Creates a new ConfigMap from a pre-existing set of mapped config fields.
     * <p>Such {@link ConfigFieldInformation} map is created with the {@link ConfigUtil} class
     * and implicitly by the other static creation methods in this class.
     *
     * @param configFields the field to config field information map
     * @return the config map that was created from the given config field map
     */
    public static <TConfig> ConfigMap<TConfig> of(Class<TConfig> configClass, Map<String, ConfigFieldInformation> configFields) {
        return new ConfigMap<>(configClass, configFields);
    }

    Class<TConfig> configClass;
    Map<String, ConfigFieldInformation> configFields;
    List<KeyValuePair> keyValuePairs;
    @NonFinal TConfig instance;

    @SuppressWarnings("unchecked")
    protected ConfigMap(TConfig config, Map<String, ConfigFieldInformation> configFields) {
        this((Class<TConfig>) config.getClass(), configFields);
        this.instance = config;
    }

    protected ConfigMap(Class<TConfig> configClass, Map<String, ConfigFieldInformation> configFields) {
        this.configClass = configClass;
        this.configFields = Map.copyOf(configFields);
        this.keyValuePairs = List.copyOf(new ArrayList<>());
    }

    protected ConfigMap(Class<TConfig> configClass, Map<String, ConfigFieldInformation> configFields, List<KeyValuePair> keyValuePairs) {
        this.configClass = configClass;
        this.configFields = Map.copyOf(configFields);
        this.keyValuePairs = List.copyOf(keyValuePairs);
    }

    public ConfigMap<TConfig> instance(TConfig config) {
        instance = config;
        return this;
    }

    public Map<String, ConfigFieldInformation> configFields() {

        return Map.copyOf(configFields);
    }

    public List<KeyValuePair> keyValuePairs() {

        return List.copyOf(keyValuePairs);
    }

    public TConfig applyTo(@NonNull TConfig config) throws ConfigurationException {
        setConfigFields(config, ConfigUtil.loadConfigValues(configFields(), keyValuePairs()));
        return config;
    }

    public TConfig apply() {
        return create();
    }

    public TConfig create() {
        try {
            if (instance() != null)
                return applyTo(instance());
            return applyTo(configClass().getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ConfigurationException("Unable to create instance of config class "
                    + configClass.getCanonicalName() + ": " + e.getMessage(), e);
        }
    }

    public ConfigMap<TConfig> with(@NonNull Collection<KeyValuePair> pairs) {

        List<KeyValuePair> values = Stream.concat(keyValuePairs().stream(), pairs.stream())
                .distinct()
                .collect(Collectors.toList());
        return new ConfigMap<>(configClass(), configFields(), values).instance(instance());
    }

    public ConfigMap<TConfig> with(@NonNull KeyValuePair... pairs) {

        return with(Arrays.asList(pairs));
    }

    private void setConfigFields(Object config, Map<ConfigFieldInformation, Object> fieldValueMap) {
        fieldValueMap.forEach((configFieldInformation, o) -> setConfigField(config, configFieldInformation, o));
    }

    private void setConfigField(Object config, ConfigFieldInformation fieldInformation, Object value) {

        try {
            if (fieldInformation.identifier().contains(".")) {
                // handle nested config objects
                String nestedIdentifier = StringUtils.substringBefore(fieldInformation.identifier(), ".");
                Field parentField = ReflectionUtil.getDeclaredField(config.getClass(), nestedIdentifier)
                        .orElseThrow(() -> new NoSuchFieldException(fieldInformation.name()));
                parentField.setAccessible(true);
                Object nestedConfigObject = parentField.get(config);
                setConfigField(
                        nestedConfigObject,
                        fieldInformation.withIdentifier(StringUtils.substringAfter(fieldInformation.identifier(), ".")),
                        value
                );
            } else {
                Field field = ReflectionUtil.getDeclaredField(config.getClass(), fieldInformation.name())
                        .orElseThrow(() -> new NoSuchFieldException(fieldInformation.name()));
                field.setAccessible(true);
                field.set(config, ReflectionUtil.toObject(fieldInformation.type(), value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
