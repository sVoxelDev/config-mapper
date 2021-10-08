package net.silthus.configmapper.bukkit;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.silthus.configmapper.ConfigFieldInformation;
import net.silthus.configmapper.ConfigUtil;
import net.silthus.configmapper.ConfigurationException;
import net.silthus.configmapper.ConfigMap;
import net.silthus.configmapper.KeyValuePair;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@EqualsAndHashCode(callSuper = true)
public class BukkitConfigMap<TConfig> extends ConfigMap<TConfig> {

    public static <TConfig> BukkitConfigMap<TConfig> of(Class<TConfig> configClass) throws ConfigurationException {

        return new BukkitConfigMap<>(configClass, ConfigUtil.getConfigFields(configClass));
    }

    public static <TConfig> BukkitConfigMap<TConfig> of(Class<TConfig> configClass, Supplier<TConfig> supplier) throws ConfigurationException {

        TConfig config = supplier.get();
        return new BukkitConfigMap<>(config, ConfigUtil.getConfigFields(config));
    }

    public static <TConfig> BukkitConfigMap<TConfig> of(TConfig config) throws ConfigurationException {

        return new BukkitConfigMap<>(config, ConfigUtil.getConfigFields(config));
    }

    public static <TConfig> BukkitConfigMap<TConfig> of(Class<TConfig> configClass, Map<String, ConfigFieldInformation> configFields) {
        return new BukkitConfigMap<>(configClass, configFields);
    }

    private BukkitConfigMap(TConfig config, Map<String, ConfigFieldInformation> configFields) {
        super(config, configFields);
    }

    private BukkitConfigMap(Class<TConfig> configClass, Map<String, ConfigFieldInformation> configFields) {

        super(configClass, configFields);
    }

    private BukkitConfigMap(Class<TConfig> configClass, Map<String, ConfigFieldInformation> configFields, List<KeyValuePair> keyValuePairs) {

        super(configClass, configFields, keyValuePairs);
    }

    public BukkitConfigMap<TConfig> with(ConfigurationSection config) {

        Stream<KeyValuePair> configValues = config.getKeys(true).stream()
                .map(s -> KeyValuePair.of(s, config.get(s)));

        List<KeyValuePair> values = Stream.concat(configValues, keyValuePairs().stream())
                .distinct()
                .collect(Collectors.toList());

        return (BukkitConfigMap<TConfig>) new BukkitConfigMap<>(configClass(), configFields(), values).instance(instance());
    }
}
