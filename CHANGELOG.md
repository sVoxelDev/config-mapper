## [1.3.5](https://github.com/Silthus/config-mapper/compare/v1.3.4...v1.3.5) (2021-10-08)


### Bug Fixes

* **release:** run explicit gradle build task ([b2f5131](https://github.com/Silthus/config-mapper/commit/b2f513169aefd6033061835e371b12379d04a77f))

## [1.3.4](https://github.com/Silthus/config-mapper/compare/v1.3.3...v1.3.4) (2021-10-08)


### Bug Fixes

* **release:** jitpack execute build instead of gradle ([54f9a75](https://github.com/Silthus/config-mapper/commit/54f9a750133d422c0273028113c9334eae3ab4c8))

## [1.3.3](https://github.com/Silthus/config-mapper/compare/v1.3.2...v1.3.3) (2021-10-08)


### Bug Fixes

* **release:** make gradle wrapper executable ([d16bd1a](https://github.com/Silthus/config-mapper/commit/d16bd1ac2e9be5a1b85fba40e60bf2a0aca2f0f0))

## [1.3.2](https://github.com/Silthus/config-mapper/compare/v1.3.1...v1.3.2) (2021-10-08)


### Bug Fixes

* **release:** use jdk16 to publish jitpack release ([a42ee71](https://github.com/Silthus/config-mapper/commit/a42ee7114556c1bb733a02a5224d2237a4bb3dec))

## [1.3.1](https://github.com/Silthus/config-mapper/compare/v1.3.0...v1.3.1) (2021-10-08)


### Bug Fixes

* **release:** ensure jitpack jdk 16 ([f6b2f08](https://github.com/Silthus/config-mapper/commit/f6b2f08c0d7c07fd4048c4328d4a352d25238871))

# [1.3.0](https://github.com/Silthus/config-mapper/compare/v1.2.2...v1.3.0) (2021-10-08)


### Features

* upgrade to jdk 17 ([e36ce48](https://github.com/Silthus/config-mapper/commit/e36ce484daad2c4ee5aef9db110c86e982ea9a18))

## [1.2.2](https://github.com/Silthus/config-mapper/compare/v1.2.1...v1.2.2) (2021-10-08)


### Bug Fixes

* pin test dependencies ([2ea08ea](https://github.com/Silthus/config-mapper/commit/2ea08ea737c45c942641608917cd23fa4243d846))

## [1.2.1](https://github.com/Silthus/config-mapper/compare/v1.2.0...v1.2.1) (2021-03-05)


### Bug Fixes

* deeply nested config fields not parsed ([a12057d](https://github.com/Silthus/config-mapper/commit/a12057da6201861343905eca9107938a3b0e3f06))

# [1.2.0](https://github.com/Silthus/config-mapper/compare/v1.1.4...v1.2.0) (2021-03-02)


### Features

* make the ConfigurationException a RuntimeException ([a90bb15](https://github.com/Silthus/config-mapper/commit/a90bb15410ea76a21601d890f8aaae1144ba686d))

## [1.1.4](https://github.com/Silthus/config-mapper/compare/v1.1.3...v1.1.4) (2020-12-19)


### Bug Fixes

* map primitive type to string and back ([6ac9d0b](https://github.com/Silthus/config-mapper/commit/6ac9d0b642fe3643d9837a9b739db9a1e70f1440))

## [1.1.3](https://github.com/Silthus/config-mapper/compare/v1.1.2...v1.1.3) (2020-12-19)


### Bug Fixes

* cast field value to type before setting it and catch all exceptions ([7d3bdb2](https://github.com/Silthus/config-mapper/commit/7d3bdb28b5d19c109b7ea427dd1a319deb28e11f))
* convert object to primitive type if needed ([82cfa06](https://github.com/Silthus/config-mapper/commit/82cfa0612e64169302558e7191af9b638473111c))

## [1.1.2](https://github.com/Silthus/config-mapper/compare/v1.1.1...v1.1.2) (2020-12-07)


### Bug Fixes

* **release:** set jitpack to jre11 ([728d5e8](https://github.com/Silthus/config-mapper/commit/728d5e8bf02e3408e0ccc5a55b339f867b20aa36))

## [1.1.1](https://github.com/Silthus/config-mapper/compare/v1.1.0...v1.1.1) (2020-12-07)


### Bug Fixes

* **bukkit:** make BukkitConfigMap public visible ([0f0eef8](https://github.com/Silthus/config-mapper/commit/0f0eef811579ff3db65e39115f6180e7054ec53f))

# [1.1.0](https://github.com/Silthus/config-mapper/compare/v1.0.2...v1.1.0) (2020-12-07)


### Features

* **docs:** add javadocs to the entrypoint ([8e35993](https://github.com/Silthus/config-mapper/commit/8e3599354d854f49c98164e7b79664adaf30821a))

## [1.0.2](https://github.com/Silthus/config-mapper/compare/v1.0.1...v1.0.2) (2020-12-07)


### Bug Fixes

* **release:** change group id of bukkit project ([634eb6b](https://github.com/Silthus/config-mapper/commit/634eb6b5f307f4cd013d7dfa33c76acbca5ab507))

## [1.0.1](https://github.com/Silthus/config-mapper/compare/v1.0.0...v1.0.1) (2020-12-07)


### Bug Fixes

* **release:** commit correct readme into release tag ([62dd592](https://github.com/Silthus/config-mapper/commit/62dd592e81877a776861625f9618d46e1f9e3f34))

# 1.0.0 (2020-12-07)


### Bug Fixes

* **release:** grant execute rights to update-versions ([6b4fe5f](https://github.com/Silthus/config-mapper/commit/6b4fe5fd36ae18273517532fb6e338544124e69a))
* **release:** use correct lowercased Readme.md name ([4d85953](https://github.com/Silthus/config-mapper/commit/4d859534c2104da4797f3a809ce71e011558c4ca))


### Features

* add BukkitConfigMap that allows mapping of ConfigurationSections to objects ([314cfbd](https://github.com/Silthus/config-mapper/commit/314cfbdd5ad1113d9540c85ed4cab25edc407b80))
* initial migration of the config mapper from the art-framework ([9e00afe](https://github.com/Silthus/config-mapper/commit/9e00afe7544cabe39765fdc7a8d02df2f50bff80))
