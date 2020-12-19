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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.NonFinal;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@NonFinal
@Accessors(fluent = true)
public class DefaultConfigMap implements ConfigMap {

    @Getter
    Map<String, ConfigFieldInformation> configFields;
    @Getter
    List<KeyValuePair> keyValuePairs;

    protected DefaultConfigMap(Map<String, ConfigFieldInformation> configFields) {
        this.configFields = ImmutableMap.copyOf(configFields);
        this.keyValuePairs = ImmutableList.of();
    }

    protected DefaultConfigMap(Map<String, ConfigFieldInformation> configFields, List<KeyValuePair> keyValuePairs) {
        this.configFields = ImmutableMap.copyOf(configFields);
        this.keyValuePairs = ImmutableList.copyOf(keyValuePairs);
    }

    @Override
    public boolean loaded() {

        return !keyValuePairs().isEmpty();
    }

    @Override
    public <TConfig> TConfig applyTo(@NonNull TConfig config) throws ConfigurationException {
        if (!this.loaded()) return config;
        setConfigFields(config, ConfigUtil.loadConfigValues(configFields(), keyValuePairs()));
        return config;
    }

    @Override
    public ConfigMap with(@NonNull Collection<KeyValuePair> keyValuePairs) {

        List<KeyValuePair> values = Stream.concat(keyValuePairs().stream(), keyValuePairs.stream())
                .distinct()
                .collect(Collectors.toList());
        return new DefaultConfigMap(configFields(), values);
    }

    @Override
    public ConfigMap with(@NonNull KeyValuePair... pairs) {

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
                Field parentField = config.getClass().getDeclaredField(nestedIdentifier);
                parentField.setAccessible(true);
                Object nestedConfigObject = parentField.get(config);
                setConfigField(nestedConfigObject, fieldInformation.withIdentifier(nestedIdentifier), value);
            } else {
                Field field = config.getClass().getDeclaredField(fieldInformation.name());
                field.setAccessible(true);
                field.set(config, ReflectionUtil.toObject(fieldInformation.type(), value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
