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
    static ConfigMap of(Class<?> configClass) throws ConfigurationException {

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
    static <TConfig> ConfigMap of(Class<TConfig> configClass, Supplier<TConfig> supplier) throws ConfigurationException {

        return of(ConfigUtil.getConfigFields(configClass, supplier.get()));
    }

    static <TConfig> ConfigMap of(TConfig config) throws ConfigurationException {

        return of(ConfigUtil.getConfigFields(config));
    }

    static ConfigMap of(Map<String, ConfigFieldInformation> configFields) {
        return new DefaultConfigMap(configFields);
    }

    <TConfig> TConfig applyTo(@NonNull TConfig config) throws ConfigurationException;

    Map<String, ConfigFieldInformation> configFields();

    List<KeyValuePair> keyValuePairs();

    boolean loaded();

    ConfigMap with(@NonNull Collection<KeyValuePair> keyValuePairs);

    ConfigMap with(@NonNull KeyValuePair... pairs);
}
