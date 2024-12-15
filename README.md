# Milkomeda
![tag](https://img.shields.io/github/tag/yizzuide/Milkomeda.svg) [![Maven Central](https://img.shields.io/maven-central/v/com.github.yizzuide/milkomeda-spring-boot-starter)](https://search.maven.org/search?q=g:com.github.yizzuide%20AND%20a:milkomeda-spring-boot-starter) ![Java CI](https://github.com/yizzuide/Milkomeda/workflows/Java%20CI/badge.svg?branch=master) [![Code Coverage](https://codecov.io/gh/yizzuide/Milkomeda/branch/master/graph/badge.svg)](https://codecov.io/gh/yizzuide/Milkomeda/branch/master) [![Production Ready](https://img.shields.io/badge/production-ready-blue.svg)](https://github.com/yizzuide/Milkomeda) ![license](https://img.shields.io/github/license/yizzuide/Milkomeda.svg)

The project name is inspired by the fact that the Milky Way and Andromeda galaxies will merge in the future, and like the Spring ecosystem, this project has a number of popular modular solutions. 

> This project is not demo or template, can be called "springboot plus" (sounds like a great project), each module needs to be enabled with the appropriate annotations.

[中文文档](https://github.com/yizzuide/Milkomeda/blob/master/README_zh-CN.md)

## Goals
- Use meta-configuration whenever possible.
- Use annotations whenever possible.
- Use declarative programming whenever possible.

## Modules
- [x] Pulsar: Used for long polling, fast-timeout. *0.1.0+*
   * Depends on: Spring MVC
- [x] Comet: Used for uniform request facet logging (include controller layer and service layer (*1.12.0+*)). *0.2.0+*
   * Depends on: Spring MVC
- [x] Pillar: Used for if/else business block splitting. *0.2.0+*
   * Depends on: Spring IoC
- [x] Particle: Support idempotent, times and Bloom limiter (*3.9.0*), also allow an extensible limiter chain of responsibility. *1.5.0+*
   * Depends on: Spring MVC, SpringBoot Data Redis
- [x] Light: Used for data cache, support super cache (ThreadLocal), L1 cache（RAM), L2 cache (Redis). *1.8.0+*
   * Depends on: SpringBoot Data Redis
- [x] Fusion: Used for dynamic modify method return value, invoke methods based on conditions (support fallback when condition is disallow). *1.12.0+*
   * Depends on: Spring AOP
- [x] Echo: Integrating the `RestTemplate` with support sign/unsigned with RSA, encryption with AES, uniform response and verification . *1.13.0+*
   * Depends on: Spring MVC
- [x] Crust: Used for both session login and jwt Token stateless login, support config token encryption with RSA and AES. *1.14.0+*
   * Depends on: Spring Security
- [x] Ice: Used for delay queue, support delay bucket, TTR, task auto schedule. *1.15.0+*
   * Depends on: Spring IoC, Spring Task, SpringBoot Data Redis
- [x] Neutron: Used for time task，support create/delete/update job online. *1.18.0+*
   * Depends on: Spring IoC, Quartz
- [x] Moon: Used for polling between multiple type values, support for concurrent operations, support for generic data values, and isolation of different services. *2.2.0+*
  * Depends on: Spring IoC, SpringBoot Data Redis
- [x] Halo: Used for listen Mybatis CRUD. *2.5.0+*
  * Depends on: Mybatis
- [x] Hydrogen: Used for aop transactions, unified exception response handling (also support for custom exceptions), parameter validation, internationalization, add interceptors and filters dynamically (support for loaded online). *3.0.0+*
  * Depends on: Spring MVC
- [x] Atom: Used for distributed lock, support both Redis and Zookeeper solution. *3.3.0+*
  * Depends on: Redission, Curator-Recipes
- [x] Wormhole: The event bus designed based on DDD architecture module can be used for event flow, big data business event production and output. *3.3.0+*
  * Depends on: Spring IoC
- [x] Sundial: Used for master-slave data source switching, addition supports sub-table with consistency of the Hash (3.8.0+). *3.4.0+*
  * Depends on: Spring JDBC, Mybatis
- [x] Jupiter: Lightweight rule engine based on data source queries, query filtering with request domains extract expression (such as `$params`, `$attr`, `$header`)，and result matching with EL/OGNL expression parsing. *3.5.0+*
  * Depends on: Spring JDBC, Spring EL, OGNL
- [x] Metal: Used for distributed dynamic configuration refresh based on KV data and support property-binding injection. *3.6.0+*
  * Depends on: Spring IoC, SpringBoot Data Redis
- [x] Orbit: Add method interceptor only with yml config or using `@Orbit`. *3.13.0+*
  * Depends on: Spring AOP
- [x] Sirius: Mybatis-plus addition support, such as page query with `@QueryMatcher`, remove before check ref with `@RefMatcher`. *3.14.0+*
  * Depends on: Mybatis-plus
    
## Requirements
* Java 8
* Lombok 1.18.x
* SpringBoot 2.x

## Version control guidelines
| Milkomeda                     | Spring Boot       |
|-------------------------------|-------------------|
| 3.15.x - 3.20.x               | 2.7.x             |
| 3.14.x                        | 2.6.x             |
| 3.13.x                        | 2.4.x - 2.5.x     |
| 3.0 - 3.12.x                  | 2.2.x - 2.3.x     |
| 1.16.x - 2.x                  | 2.1.x             |
| Dalston.1.11.0-Dalston.1.12.0 | Spring Boot 1.5.x |

## Installation
```xml
<dependency>
    <groupId>com.github.yizzuide</groupId>
    <artifactId>milkomeda-spring-boot-starter</artifactId>
    <version>${milkomeda-last-version}</version>
</dependency>
```

## Upgrade
### 3.0 Release
Milkomeda 3.0 is now available（April 2020). 

- YML config, promoted to first-class of development, allows developers to start quickly.
- Add a new module called `Hydrogen`, the infrastructure functions of Spring and SpringMVC are all taken over, and explored from the source level.
- The aggregation ability of functional modules has strengthened to achieve the improvement of the small world within the big world.

### [More](https://github.com/yizzuide/Milkomeda/wiki/Upgrade-Guide)

## Documentation
[See Wiki](https://github.com/yizzuide/Milkomeda/wiki)

## Releases log
[See Releases](https://github.com/yizzuide/Milkomeda/releases)

## Contributing
This project needs more developers to work together, join us with some notice as the following:

- If you have a better idea, please submit a new issue with `[feature]` prefix.
- Fork from `master` branch, check out a new branch named like `feat_module_yyyyMMdd` for add new module，`fix_module_yyyyMMdd` for fixing issues.
- The newly added module should be as universal as possible, also provide demo in `MilkomedaDemo` as better.
- The newly added module must be under package `com.github.yizzuide.milkomeda` with addition module name subpackage, and can be enabled with annotation in Spring Boot environment.
- Don't commit IDE config file and OS temp file, please add to `.gitignore` file.
- The last thing, pull request for me with `master` branch or `dev-xxx` if there is a recent new version development branch.

## Author
yizzuide fu837014586@163.com

## License
Milkomeda is available under the MIT license. See the LICENSE file for more info.

## Sponsors
<a href="https://www.jetbrains.com" target="_blank">
  <img width="100" src="./logo/sponsors.png" alt="IntelliJ IDEA">
</a>

