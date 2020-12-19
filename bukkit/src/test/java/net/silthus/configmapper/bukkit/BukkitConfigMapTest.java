package net.silthus.configmapper.bukkit;

import lombok.Data;
import lombok.SneakyThrows;
import net.silthus.configmapper.ConfigOption;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BukkitConfigMapTest {

    @SneakyThrows
    @Test
    @DisplayName("should map configuration section to object")
    void shouldMapConfigSectionToObject() {

        MemoryConfiguration config = new MemoryConfiguration();
        config.set("required", "foobar");
        config.set("val", 10L);

        BukkitConfig result = BukkitConfigMap.of(BukkitConfig.class)
                .with(config)
                .applyTo(new BukkitConfig());

        assertThat(result).extracting(
                BukkitConfig::getRequired,
                BukkitConfig::getVal
        ).contains("foobar", 10);
    }

    @Data
    public static class BukkitConfig {

        @ConfigOption(required = true)
        private String required;
        @ConfigOption
        private int val = 5;
    }
}