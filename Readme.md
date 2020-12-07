# Config Mapper

[![Build Status](https://github.com/Silthus/config-mapper/workflows/Build/badge.svg)](../../actions?query=workflow%3ABuild)
[![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/github/v/release/Silthus/config-mapper?include_prereleases&label=release)](../../releases)
[![codecov](https://codecov.io/gh/Silthus/config-mapper/branch/master/graph/badge.svg)](https://codecov.io/gh/Silthus/config-mapper)
[![Commitizen friendly](https://img.shields.io/badge/commitizen-friendly-brightgreen.svg)](http://commitizen.github.io/cz-cli/)
[![semantic-release](https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg)](https://github.com/semantic-release/semantic-release)

Use this library to map between configurations and plain java objects. The difference between this project and others, like jackson, is that you can configure what is required in what position and so on.
This allows you to write your own parser for the configuration and then map it into your objects.

An example of this is the [art-framework](https://art-framework.io) which uses this library to parse and validate its configuration strings.

* [As a Dependency](#as-a-dependency)
  * [Gradle](#gradle)
  * [Maven](#maven)
* [Usage](#usage)

## As a Dependency

Include `config-mapper` in your project and shade it via gradle or maven.

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation group: 'net.silthus', name: 'config-mapper', version: '1.0.0'
}
```

### Maven

```xml
<project>
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>net.silthus</groupId>
            <artifactId>config-mapper</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
</project>
```

## Usage

You can pass any class to the config mapper and create a `ConfigMap` which can then be applied to your objects.

```java
try {
    // 1. create a config map from all annotated @ConfigOption fields in your class
    ConfigMap configMap = ConfigMap.of(YourClass.class);
    // 2. load the values that should be mapped to the fields
    //    this is the part where you can create your custom parser
    //    or use one of the parsers that comes with this project
    // ~ the ConfigMap object is immutable so we need to reassign it
    configMap = configMap.with(
        KeyValuePair.of("my_config_key", "my-value"),
        KeyValuePair.of("second_key", 2.0)
    );
    // 3. apply all values to your object and check if all prerequesites are met (like required settings)
    YourClass config = configMap.applyTo(new YourClass());
} catch(ConfigurationException e) {
    // something went wrong while scanning your class for config fields
    // the error message contains details about the error
}

public static class YourClass {

    // make sure your class has a public parameterless constructor
    // or use the ConfigMap.of(Class<?>, Supplier<?>) method
    public YourClass() {}

    @ConfigOption
    private String myConfigKey = "foobar";
    @ConfigOption(required = true)
    private double secondKey;
}
```
