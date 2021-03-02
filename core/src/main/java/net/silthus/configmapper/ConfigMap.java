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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
public interface ConfigMap {

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
    static ConfigMap of(Class<?> configClass) {

        return of(ConfigUtil.getConfigFields(configClass));
    }

    /**
     * Creates a new ConfigMap using the given supplier to create the config object.
     * <p>This is the alternative to the {@link #of(Class)} method that does not require a
     * public parameterless constructor.
     *
     * @param configClass the config class that should be analyzed for configured fiels
     * @param supplier the supplier used to create a fresh instance of the config class
     * @param <TConfig> the type of the config
     * @return a ConfigMap of all fields inside the given class
     * @throws ConfigurationException if the config class contains invalid field mappings
     * @see #of(Class)
     */
    static <TConfig> ConfigMap of(Class<TConfig> configClass, Supplier<TConfig> supplier) {

        return of(ConfigUtil.getConfigFields(configClass, supplier.get()));
    }

    /**
     * Creates a new ConfigMap using the provided config instance to map field default values.
     * <p>No new instance of the config will be created by this method.
     *
     * @param config the config object that should be scanned for config fields
     * @param <TConfig> the type of the config
     * @return the config map for the given config object
     */
    static <TConfig> ConfigMap of(TConfig config) {

        return of(ConfigUtil.getConfigFields(config));
    }

    /**
     * Creates a new ConfigMap from a pre-existing set of mapped config fields.
     * <p>Such {@link ConfigFieldInformation} map is created with the {@link ConfigUtil} class
     * and implicitly by the other static creation methods in this class.
     *
     * @param configFields the field to config field information map
     * @return the config map that was created from the given config field map
     */
    static ConfigMap of(Map<String, ConfigFieldInformation> configFields) {
        return new DefaultConfigMap(configFields);
    }

    /**
     * @return an immutable map of all field names to config fields mapping in this config map
     */
    Map<String, ConfigFieldInformation> configFields();

    /**
     * @return an immutable list of all key value pairs in this config map
     */
    List<KeyValuePair> keyValuePairs();

    /**
     * @return true if values have been provided to this config map
     */
    boolean loaded();

    /**
     * Applies all mapping information and values in this config map to the given configuration object.
     * <p>Only the default values will be applied if {@link #with(KeyValuePair...)} was not called yet.
     * <p>Any nested objects that are marked as {@link ConfigOption} will be injected recursively.
     *
     * @param config the config to apply the values to
     * @param <TConfig> the type of the config
     * @return the same config instance but with the injected config values
     */
    <TConfig> TConfig applyTo(@NonNull TConfig config);

    /**
     * Provides this config map with the given values.
     * <p>Use a provided sub package, like the bukkit implementation to parse a config into a list of key value pairs.
     *
     * @param pairs the key value pairs that should be mapped to this config
     * @return a new config map with the given values
     */
    ConfigMap with(@NonNull Collection<KeyValuePair> pairs);

    /**
     * Provides this config map with the given values.
     * <p>Use a provided sub package, like the bukkit implementation to parse a config into a list of key value pairs.
     *
     * @param pairs the key value pairs that should be mapped to this config
     * @return a new config map with the given values
     */
    ConfigMap with(@NonNull KeyValuePair... pairs);
}
