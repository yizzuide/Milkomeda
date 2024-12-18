/*
 * Copyright (c) 2024 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.demo.wormhole.application.service;

import com.github.yizzuide.milkomeda.demo.wormhole.domain.aciton.Actions;
import com.github.yizzuide.milkomeda.demo.wormhole.appearance.command.AuditCommand;
import com.github.yizzuide.milkomeda.demo.wormhole.domain.model.Credit;
import com.github.yizzuide.milkomeda.wormhole.WormholeEvent;
import com.github.yizzuide.milkomeda.wormhole.WormholeHolder;
import org.springframework.stereotype.Service;

/**
 * 信用应用服务
 *
 * @author yizzuide
 * <br>
 * Create at 2020/05/05 15:38
 */
@Service
public class CreditApplicationService {

    /*
     * DDD设计与实现：
     *  1. DDD是一种架构设计方法论，通过边界划分将复杂业务领域简单化来实现架构演进。
     *  2. 建立一个核心而稳定的领域模型，有利于领域知识的传递与传承。
     *  3. 领域会细分为不同的子域，子域可以根据自身重要性和功能属性划分为三类子域：
     *      1）核心域：决定产品和公司核心竞争力的子域是核心域。
     *      2）通用域：没有太多个性化的诉求通用系统（短信、认证、日志），同时被多个子域使用的通用功能。
     *      3）支撑域：具有企业特性，但不具有通用性系统（数据字典，站内信）。
     *  4. 事件风暴是建立领域模型的主要方法，它是一个从发散（产生很多的实体、命令、事件等领域对象）到收敛（建立领域模型与限界上下文）的过程。
     *  5. 在事件风暴过程中，能够简单、清晰、准确描述业务涵义和规则的语言就是通用语言，而这个语言所在的语义环境则是由限界上下文来限定的，以确保语义的唯一性。
     *  6. 通用语言中的名词可以给领域对象命名，如商品、订单等，对应实体对象；而动词则表示一个动作或事件，如商品已下单、订单已付款等，对应领域事件或者命令。
     *  7. 限界上下文（BoundedContext）：限界就是领域的边界，而上下文则是语义环境，用来封装通用语言和领域对象。一个限界上下文里通常有多个聚合，聚合逻辑上是相对独立的。
     *      1）在DDD实践中，聚合是事务的边界；聚合之间并不保证事务，只能用最终一致性。任何需要事务保护的逻辑都应该在一个聚合内。
     *      2）在限界上下文里，将其他聚合能力整合在一起对外提供能力的聚合，被称为聚合根；其他聚合也被称为实体。
     *  8. 限界上下文确定了微服务的设计和拆分方向，一般来说一个限界上下文拆分成一个微服务，但还需要考虑服务的粒度、分层、边界划分、依赖关系和集成关系。
     *  9. 实体（Entity）：拥有唯一标识符，且标识符在历经各种状态变更后仍能保持一致，领域模型中的实体是多个属性、操作或行为的载体，它和值对象是组成领域模型的基础单元。
     *  10. 在事件风暴中，我们可以根据命令、操作或者事件，找出产生这些行为的业务实体对象，进而按照一定的业务规则将依存度高和业务关联紧密的多个实体对象和值对象进行聚类，形成聚合。
     *  11. 在DDD里，这些实体类通常采用充血模型，与这个实体相关的所有业务逻辑都在实体类的方法中实现，跨多个实体的领域逻辑则在领域服务（DomainService）中实现。
     *  12. 与传统数据模型设计优先不同，DDD是先构建领域模型，针对实际业务场景构建实体对象和行为，再将实体对象映射到数据持久化对象。
     *  13. 一个实体可能对应0个或多个持久化对象，但大多是一对一、一对多和多对一的关系，而有些实体只是暂驻静态内存（如：多个价格配置数据计算后生成的折扣实体）。
     *  14. 值对象（ValueObject）：通过对象属性值来识别的对象（一个没有标识符的对象），它将多个相关属性组合为一个概念整体，且是不可变的。
     *  15. 如果值对象是单一属性，则直接定义为实体类的属性；如果值对象是属性集合，则把它设计为复合类型。
     *  16. 聚合就是由业务和逻辑紧密关联的实体和值对象组合而成的，里面一定有一个实体是聚合根。聚合是数据修改和持久化的基本单元，每一个聚合对应一个仓储，实现数据的持久化。
     *  17. 聚合有一个聚合根和上下文边界，这个边界根据业务单一职责和高内聚原则，定义了聚合内部应该包含哪些实体和值对象，而聚合之间的边界是松耦合的。
     *  18. 聚合内有一套不变的业务规则，各实体和值对象按照统一的业务规则运行，封装真正的不变性实现对象数据的一致性。
     *  19. 聚合在DDD分层架构里属于领域层，领域层包含了多个聚合，共同实现核心业务逻辑。跨多个实体的业务逻辑通过领域服务来实现，跨多个聚合的业务逻辑通过应用服务来实现（包括微服务中）。
     *  20. 聚合根（AggregateRoot）：如果把聚合比作组织，那聚合根就是这个组织的负责人。聚合根也称为根实体，它不仅是实体，还是聚合的管理者。
     *  21. 聚合根还是聚合对外的接口人，以聚合根ID关联的方式接受外部任务和请求，在上下文内实现聚合之间的业务协同。
     *  22. 如果聚合设计得过大，聚合会因为包含过多的实体，导致实体之间的管理过于复杂，高频操作时会出现并发冲突或者数据库锁，最终导致系统可用性变差。
     *  23. 聚合之间是通过关联外部聚合根ID的方式引用，而不是直接对象引用的方式，因为直接对象容易造成边界不清晰，也会增加聚合之间的耦合度。
     *  24. 聚合内数据强一致性，而聚合之间数据最终一致性。如果一次业务操作涉及多个聚合状态的更改，应采用领域事件的方式异步修改相关的聚合，实现聚合之间的解耦。
     *  25. 领域事件：一个领域事件将导致进一步的业务操作，也可能是定时批处理过程中发生的事件。在实现业务解耦的同时，还有助于形成完整的业务闭环。
     *  26. 领域事件的执行需要一系列的组件和技术来支撑，包括：事件构建和发布、事件数据持久化、事件总线、消息中间件、事件接收和处理等。
     *  27. 事件基本属性至少包括：事件唯一标识、发生时间、事件类型和事件源。还有一项是业务属性，记录事件发生那一刻的业务数据，它会随事件传输到订阅方，以开展下一步的业务操作。
     *  28. 领域事件发生后，事件中的业务数据不再修改，因此业务数据可以以序列化值对象的形式保存，这种存储格式在消息中间件中也比较容易解析和获取。
     *  29. 事件发布的方式可以通过应用服务或者领域服务发布到事件总线或者消息中间件，也可以从事件表中利用定时程序或数据库日志捕获技术获取增量事件数据，发布到消息中间件。
     *  30. 事件数据持久化可用于系统之间的数据对账，或者实现发布方和订阅方事件数据的审计。
     *  31. 事件数据持久化有两种方案：
     *      1）持久化到本地业务数据库的事件表中，利用本地事务保证业务和事件数据的一致性。
     *      2）持久化到共享的事件数据库中。它们的数据持久化操作会跨数据库，因此需要分布式事务机制来保证业务和事件数据的强一致性。
     *  32. 事件总线(EventBus)：事件总线是实现微服务内聚合之间领域事件的重要组件，它提供事件分发和接收等服务。
     *  33. 事件分发流程:
     *      1）如果是微服务内的订阅者（其它聚合），则直接分发到指定订阅者；
     *      2）如果是微服务外的订阅者，将事件数据保存到事件库（表）并异步发送到消息中间件；
     *      3）如果同时存在微服务内和外订阅者，则先分发到内部订阅者，将事件消息保存到事件库（表），再异步发送到消息中间件。
     *  34. 用户接口层：还需要考虑服务的粒度、分层、边界划分、依赖关系和集成关系。
     *  35. 应用层：应用层是很薄的一层，理论上不应该有业务规则或逻辑，主要面向用例和流程相关的操作。
     *      1）应用层位于领域层之上，而领域层包含多个聚合，所以它负责协调多个聚合的服务和领域对象完成服务编排和组合，负责处理业务用例的执行顺序以及结果的拼装。
     *      2）应用层也是微服务之间交互的通道，它可以调用其它微服务的应用服务，完成微服务之间的服务组合和编排。
     *      3）应用服务还可以进行安全认证、权限校验、事务控制、发送或订阅领域事件等。
     *  36. 领域层：包含聚合根、实体、值对象、领域服务等领域模型中的领域对象。
     *      1）领域层的作用是实现企业核心业务逻辑，通过各种校验手段保证业务的正确性。
     *      2）领域模型的业务逻辑主要是由实体和领域服务来实现的，其中实体会采用充血模型来实现所有与之相关的业务功能。
     *      3）实体和领域服务在实现业务逻辑上不是同级的，当领域中的某些功能，单一实体（或者值对象）不能实现时，领域服务就会出马，
     *         它可以组合聚合内的多个实体（或者值对象），实现复杂的业务逻辑。如：多个领域对象作为输入值，结果产生一个值对象。
     *      4）领域包含限界上下文，限界上下文包含子域，子域包含聚合，聚合包含实体和值对象。
     *  37. 基础层：基础层是贯穿所有层的，它的作用就是为其它各层提供通用的技术和基础服务，包括第三方工具、驱动、消息中间件、网关、文件、缓存以及数据库等。
     *  38. DDD分层架构有一个重要的原则：每层只能与位于其下方的层发生耦合。
     */


    // 审核完成回调（一个User Case在Application Service中对应一个处理方法）
    public void audit(AuditCommand auditCommand) {
        // 审核成功
        if (auditCommand.getState() == 0) {
            // 保存订单状态...

            // 模拟从Repository查询用户信用聚合根
            Credit credit = new Credit();
            credit.setOrderId(auditCommand.getOrderId());
            credit.setUserId(1001L);

            // 发送审核成功事件
            WormholeHolder.getEventBus().publish(new WormholeEvent<>(this, "audit", credit), Actions.AUDIT_SUCCESS);
        }

    }
}
