# Milkomeda
![tag](https://img.shields.io/github/tag/yizzuide/Milkomeda.svg) ![license](https://img.shields.io/github/license/yizzuide/Milkomeda.svg)
名字源于未来要融合的”银河织女系“，代表当前Spring生态的全家桶体系，这个项目以Spring生态为基础，从实际业务上整理出来的快速开发模块。

目前添加的模块有：
- [x] Particle: 基于Springboot Data Redis，可用于幂等/去重、次数限制，及可扩展限制器责任链。
- [x] Comet:  基于Spring MVC，可用于统一的请求切面日志记录。
- [x] Pulsar: 基于Spring MVC的`DeferredResult`，可用于长轮询、耗时请求fast-timeout等
- [x] Pillar: 使用策略模式，可用于if/else业务块拆分。

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
1. ![Particle快速入门]()

## Author
yizzuide, fu837014586@163.com

## License
Milkomeda is available under the MIT license. See the LICENSE file for more info.

