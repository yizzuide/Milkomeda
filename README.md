# Milkomeda
![tag](https://img.shields.io/github/tag/yizzuide/Milkomeda.svg) ![license](https://img.shields.io/github/license/yizzuide/Milkomeda.svg)

名字源于未来要融合的”银河织女系“，代表当前Spring生态的全家桶体系，这个项目以Spring生态为基础，从实际业务上整理出来的快速开发模块集合，让SpringBoot开发可以更快。

> 该项目并非Demo或者框架模板工程，而是源码级提升的SpringBoot Plus，每个功能模块需要使用相应注解启用才会加载，模块多而不重，可选择性启用。

## Goals
- 能用YML配置元信息完成的，绝不会用代码配置。
- 能用注解元编程完成的，绝不会用API代码。
- 能用面向声明式编程的，绝不会用面向过程或面向对象编程。

## Modules
- [x] Pulsar（脉冲星）: 用于长轮询、耗时请求fast-timeout等。*0.1.0+*
   * 依赖技术：Spring MVC
   * 设计模式：适配器模式、代理模式
- [x] Comet（彗星）:  用于统一的请求切面日志记录（包括Controller层、Service层（*1.12.0+*））。*0.2.0+*
   * 依赖技术：Spring MVC
   * 设计模式：策略模式、装饰模式
- [x] Pillar（创生柱）: 用于if/else业务块拆分。*0.2.0+*
   * 可选依赖技术：Spring IoC
   * 设计模式：策略模式、适配器模式
- [x] Particle（粒子）: 用于幂等/去重、次数限制，及可扩展限制器责任链。*1.5.0+*
   * 依赖技术：Spring MVC、SpringBoot Data Redis
   * 设计模式：策略模式、责任链模式、组合模式
- [x] Light (光）: 用于快速缓存，支持超级缓存（ThreadLocal）、一级缓存（内存缓存池）、二级缓存（Redis)。 *1.8.0+*
   * 依赖技术：SpringBoot Data Redis
   * 设计模式：策略模式、模板方法模式、门面模式
- [x] Fusion（核聚变）：用于动态切面修改方法返回值（主要针对于定制前端响应数据格式）、根据条件启用/禁用组件方法的执行、根据条件调用可替换的反馈方法。*1.12.0+*
   * 依赖技术：Spring AOP
   * 设计模式：策略模式
- [x] Echo（回响）：用于第三方请求，支持签名/验签、数据加密、可定制统一响应类型和成功校验。*1.13.0+*
   * 依赖技术：Spring MVC
   * 设计模式：模板方法模式、适配器模式、工厂方法模式
- [x] Crust（外壳）：用于Session、JWT Token认证，支持验证、刷新Token，可选配置对称与RSA非对称生成Token，BCrypt或自定义salt表字段加密的方式。*1.14.0+*
   * 依赖技术：Spring Security
   * 设计模式：模板方法模式、适配器模式
- [x] Ice（冰）：用于延迟列队的需求，支持配置延迟分桶、任务执行超时时间（TTR）、超时重试、Task自动调度等。*1.15.0+*
   * 依赖技术：Spring IoC、Spring Task、SpringBoot Data Redis
   * 设计模式：策略模式、享元模式、门面模式、面向声明式编程
- [x] Neutron（中子星）：用于定时作业任务，支持数据库持久化，动态创建Job、删除、修改Cron执行表达式。*1.18.0+*
   * 依赖技术：Spring IoC、Quartz
   * 设计模式：门面模式
- [x] Moon（月球）：用于在多个类型值之间轮询，支持并发操作，支持泛型数据值，不同的环业务相互隔离。*2.2.0+*
  * 依赖技术：Spring IoC、SpringBoot Data Redis
  * 设计数据结构：环形链表
  * 设计模式：策略模式、门面模式
- [x] Halo（光晕）：用于监听Mybatis的CRUD操作并执行相应的业务，支持前置、后置类型。*2.5.0+*
  * 依赖技术：Mybatis
  * 设计模式：面向声明式编程

## TODO
- [ ] Hydrogen（氢）：用于开箱即用的切面事务、统一异常响应处理（支持自定义异常）、参数校验、国际化、动态添加拦截器（支持属性注入）。*3.0.0+*
    
## Requirements
* Java 8
* Lombok 1.18.x
* SpringBoot 2.x

## Version control guidelines
- 1.16.0+ for Spring Boot 2.1.x - 2.2.x
- Dalston.1.11.0-Dalston.1.12.0 for Spring Boot 1.5.x
- Others for Spring Boot 2.0.x

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
Milkomeda 3.0 coming soon（April 2020). 

- Yml配置提升为开发的一等公民，使开发者可以快速接入模块。
- 新增`Hydrogen`模块，对Spring、SpringMVC的基础建设功能全部包揽，从源码级发掘实用功能。
- 功能模块聚合能力加强，做到大世界内有小世界的改进。

### Migrating to 3.x from 2.x

- [Comet] 模块包由`com.github.yizzuide.milkomeda.comet`迁移到`com.github.yizzuide.milkomeda.comet.core`，因为该模块聚合了`Collector`和`Logger`。

### [More](https://github.com/yizzuide/Milkomeda/wiki/Upgrade-Guide)

## Documentation
[See Wiki](https://github.com/yizzuide/Milkomeda/wiki)

## Releases log
[See Releases](https://github.com/yizzuide/Milkomeda/releases)

## Contributing
*Mikomeda*还需要更多的业务型实用模块，欢迎有想法的开发者加入进来！可以通过以下原则来Pull Request:

- 从`master`分支拉取新分支，新添加的功能模块分支以`feat_模块名_yyyyMMdd`形式命名，问题修复则以`fix__yyyyMMdd`模块名形式命名。
- 添加的模块尽可能地通用，不能含有业务代码，最好可以提供使用的Demo并添加到`MilkomedaDemo`工程里，如果有好的想法需要讨论可以提一个以[feature]开头的issue进行讨论。
- 新添加的代码尽可能地规范，代码必需经过格式化，类的命名需要添加模块名前缀，新添加的模块需要添加到`Milkomeda`的`com.github.yizzuide.milkomeda`包下，属性和方法需要添加注释表明如何使用。
- 建议遵行提交注释以`feat|fix|docs|style|refactor|perf|test|workflow|ci|chore|types:`为前缀。
- 提交时不要提交IDE的配置相关文件和临时生成的文件，请注意排除。

> 关于如何开发*Mikomeda*项目：使用IDEA新建空的工程，再把工程模块`Mikomeda`和`MikomedaDemo`导入即可。

## Author
yizzuide, fu837014586@163.com

## License
Milkomeda is available under the MIT license. See the LICENSE file for more info.

## Thanks
<a href="https://www.jetbrains.com/idea/" target="_blank">
  <img width="64px" src="./logo/idea.png" alt="IntelliJ IDEA">
</a>

