package net.silthus.configmapper.bukkit;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.silthus.configmapper.ConfigFieldInformation;
import net.silthus.configmapper.ConfigUtil;
import net.silthus.configmapper.ConfigurationException;
import net.silthus.configmapper.DefaultConfigMap;
import net.silthus.configmapper.KeyValuePair;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@EqualsAndHashCode(callSuper = true)
public class BukkitConfigMap extends DefaultConfigMap {

    public static BukkitConfigMap of(Class<?> configClass) throws ConfigurationException {

        return of(ConfigUtil.getConfigFields(configClass));
    }

    public static <TConfig> BukkitConfigMap of(Class<TConfig> configClass, Supplier<TConfig> supplier) throws ConfigurationException {

        return of(ConfigUtil.getConfigFields(configClass, supplier.get()));
    }

    public static <TConfig> BukkitConfigMap of(TConfig config) throws ConfigurationException {

        return of(ConfigUtil.getConfigFields(config));
    }

    public static BukkitConfigMap of(Map<String, ConfigFieldInformation> configFields) {
        return new BukkitConfigMap(configFields);
    }

    protected BukkitConfigMap(Map<String, ConfigFieldInformation> configFields) {

        super(configFields);
    }

    public BukkitConfigMap(Map<String, ConfigFieldInformation> configFields, List<KeyValuePair> keyValuePairs) {

        super(configFields, keyValuePairs);
    }

    public BukkitConfigMap with(ConfigurationSection config) {

        Stream<KeyValuePair> configValues = config.getKeys(true).stream()
                .map(s -> KeyValuePair.of(s, config.get(s)));

        List<KeyValuePair> values = Stream.concat(configValues, keyValuePairs().stream())
                .distinct()
                .collect(Collectors.toList());

        return new BukkitConfigMap(configFields(), values);
    }
}
