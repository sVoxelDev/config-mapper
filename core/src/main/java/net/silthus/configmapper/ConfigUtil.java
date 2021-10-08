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
import lombok.extern.java.Log;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

@Log(topic = "configmapper")
public final class ConfigUtil {

    public static Map<String, ConfigFieldInformation> getConfigFields(Class<?> configClass, FieldNameFormatter formatter) throws ConfigurationException {
        try {
            Constructor<?> constructor = configClass.getConstructor();
            constructor.setAccessible(true);
            return getConfigFields("", configClass, constructor.newInstance(), formatter);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ConfigurationException("Unable to create instance of config class \"" + configClass.getSimpleName() + "\": " + e.getMessage()
                    + ". Is it public and has a public no args constructor?", e);
        }
    }

    public static Map<String, ConfigFieldInformation> getConfigFields(Class<?> configClass) throws ConfigurationException {
        return getConfigFields(configClass, FieldNameFormatters.LOWER_UNDERSCORE);
    }

    public static <TConfig> Map<String, ConfigFieldInformation> getConfigFields(Class<TConfig> configClass, TConfig config) throws ConfigurationException {
        return getConfigFields(configClass, config, FieldNameFormatters.LOWER_UNDERSCORE);
    }

    public static <TConfig> Map<String, ConfigFieldInformation> getConfigFields(TConfig config) throws ConfigurationException {

        return getConfigFields("", config.getClass(), config, FieldNameFormatters.LOWER_UNDERSCORE);
    }

    public static <TConfig> Map<String, ConfigFieldInformation> getConfigFields(Class<TConfig> configClass, TConfig config, FieldNameFormatter formatter) throws ConfigurationException {
        return getConfigFields("", configClass, config, formatter);
    }

    private static Map<String, ConfigFieldInformation> getConfigFields(String basePath, Class<?> configClass, Object configInstance, FieldNameFormatter formatter) throws ConfigurationException {
        Map<String, ConfigFieldInformation> fields = new HashMap<>();

        try {
            Field[] configFields;
            if (configClass.isAnnotationPresent(ConfigOption.class)) {
                configFields = FieldUtils.getAllFields(configClass);
            } else {
                configFields = FieldUtils.getFieldsWithAnnotation(configClass, ConfigOption.class);
            }

            for (Field field : configFields) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (field.isAnnotationPresent(Ignore.class)) continue;
                if (Modifier.isFinal(field.getModifiers())) {
                    if (field.isAnnotationPresent(ConfigOption.class)) {
                        throw new ConfigurationException("Cannot use a final field as a config option. Remove the @ConfigOption or the final modifier from \"" + field.getName() + "\"");
                    }
                    continue;
                }

                Optional<ConfigOption> configOption = getConfigOption(field);

                String identifier = basePath + configOption.map(ConfigOption::value)
                        .filter(s -> !s.trim().isEmpty())
                        .orElse(formatter.apply(field.getName()));

                if (field.getType().isPrimitive() || field.getType().equals(String.class) || field.getType().isArray()) {

                    String[] description = configOption.map(ConfigOption::description).orElse(new String[0]);
                    Boolean required = configOption.map(ConfigOption::required).orElse(false);
                    Integer position = configOption.map(ConfigOption::position).orElse(-1);

                    field.setAccessible(true);

                    Object defaultValue = field.get(configInstance);

                    if (field.getType().isArray() && defaultValue == null) {
                        defaultValue = Array.newInstance(field.getType().getComponentType(), 0);
                    }

                    fields.put(identifier, new ConfigFieldInformation(
                            identifier,
                            field.getName(),
                            field.getType(),
                            position,
                            description,
                            required,
                            defaultValue
                    ));
                } else {
                    fields.putAll(getConfigFields(identifier + ".", field.getType(), field.getType().getConstructor().newInstance(), formatter));
                }
            }

            List<ConfigFieldInformation> sameFieldPosition = fields.values().stream().filter(field1 -> fields.values().stream().anyMatch(
                    field2 -> field1 != field2
                            && field1.position() > -1
                            && field2.position() > -1
                            && field1.position() == field2.position()
            )).collect(Collectors.toList());

            if (!sameFieldPosition.isEmpty()) {
                throw new ConfigurationException("found same position " + sameFieldPosition.get(0).position() + " on the following fields: "
                        + sameFieldPosition.stream().map(ConfigFieldInformation::identifier).collect(Collectors.joining(",")));
            }

        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
            throw new ConfigurationException(e);
        }

        return fields;
    }

    public static Optional<ConfigOption> getConfigOption(Field field) {

        if (field.isAnnotationPresent(ConfigOption.class)) {
            return Optional.of(field.getAnnotation(ConfigOption.class));
        }
        return Optional.empty();
    }

    public static Map<ConfigFieldInformation, Object> loadConfigValues(@NonNull Map<String, ConfigFieldInformation> configFields, @NonNull List<KeyValuePair> keyValuePairs) throws ConfigurationException {

        if (configFields.isEmpty()) return new HashMap<>();

        Map<ConfigFieldInformation, Object> fieldValueMap = new HashMap<>();
        Set<ConfigFieldInformation> mappedFields = new HashSet<>();

        boolean usedKeyValue = false;

        for (int i = 0; i < keyValuePairs.size(); i++) {
            KeyValuePair keyValue = keyValuePairs.get(i);

            if (keyValue.getKey().isPresent() && !configFields.containsKey(keyValue.getKey().get())) {
                continue;
            }

            ConfigFieldInformation configFieldInformation;
            if (keyValue.getKey().isPresent() && configFields.containsKey(keyValue.getKey().get())) {
                configFieldInformation = configFields.get(keyValue.getKey().get());
                usedKeyValue = true;
            } else if (configFields.size() == 1) {
                configFieldInformation = configFields.values().stream().findFirst().get();
            } else {
                if (usedKeyValue) {
                    throw new ConfigurationException("Positioned parameter found after key=value pair usage. Positioned parameters must come first.");
                }
                int finalI = i;
                Optional<ConfigFieldInformation> optionalFieldInformation = configFields.values().stream().filter(info -> info.position() == finalI).findFirst();
                if (optionalFieldInformation.isEmpty()) {
                    throw new ConfigurationException("Config does not define positioned parameters. Use key value pairs instead.");
                }
                configFieldInformation = optionalFieldInformation.get();
            }

            if (keyValue.getValue().isEmpty()) {
                throw new ConfigurationException("Config " + configFieldInformation.identifier() + " has an empty value.");
            }

            Object value = ReflectionUtil.toObject(configFieldInformation.type(), keyValue.getValue().get());

            fieldValueMap.put(configFieldInformation, value);
            mappedFields.add(configFieldInformation);
        }

        List<ConfigFieldInformation> missingRequiredFields = configFields.values().stream()
                .filter(ConfigFieldInformation::required)
                .filter(configFieldInformation -> !mappedFields.contains(configFieldInformation))
                .collect(Collectors.toList());

        if (!missingRequiredFields.isEmpty()) {
            throw new ConfigurationException("Config is missing " + missingRequiredFields.size() + " required parameters: "
                    + missingRequiredFields.stream().map(ConfigFieldInformation::identifier).collect(Collectors.joining(",")));
        }

        return fieldValueMap;
    }
}
