# Milkomeda
![tag](https://img.shields.io/github/tag/yizzuide/Milkomeda.svg) ![license](https://img.shields.io/github/license/yizzuide/Milkomeda.svg)

名字源于未来要融合的”银河织女系“，代表当前Spring生态的全家桶体系，这个项目以Spring生态为基础，从实际业务上整理出来的快速开发模块。

目前添加的模块有：
- [x] Particle(粒子): 用于幂等/去重、次数限制，及可扩展限制器责任链。
    * 依赖技术：Spring MVC、SpringBoot Data Redis
    * 设计模式：策略模式、责任链模式、组合模式
- [x] Comet（彗星）:  用于统一的请求切面日志记录。
    * 依赖技术：Spring MVC
    * 设计模式：策略模式
- [x] Pulsar（脉冲星）: 用于长轮询、耗时请求fast-timeout等。
    * 依赖技术：Spring MVC
    * 设计模式：适配器模式、代理模式
- [x] Pillar（创生柱）: 用于if/else业务块拆分。
    * 可选依赖技术：Spring IoC
    * 设计模式：策略模式、适配器模式

## Requirements
* Java 8
* Springboot 2.x

## Installation
```xml
<dependency>
    <groupId>com.github.yizzuide</groupId>
    <artifactId>milkomeda-spring-boot-starter</artifactId>
    <version>1.5.0</version>
</dependency>
```

## Documentation
1. [Particle快速入门](https://github.com/yizzuide/Milkomeda/wiki/1.-Particle%E5%BF%AB%E9%80%9F%E5%85%A5%E9%97%A8)

## Author
yizzuide, fu837014586@163.com

## License
Milkomeda is available under the MIT license. See the LICENSE file for more info.

