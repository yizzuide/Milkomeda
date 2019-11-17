package com.github.yizzuide.milkomeda.demo;

import com.github.yizzuide.milkomeda.crust.EnableCrust;
import com.github.yizzuide.milkomeda.ice.EnableIce;
import com.github.yizzuide.milkomeda.echo.EnableEcho;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MilkomedaDemoApplication
 *
 * @author yizzuide
 * Create at 2019/03/30 19:04
 */
@EnableIce
@EnableCrust
@EnableEcho
@SpringBootApplication
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class MilkomedaDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(MilkomedaDemoApplication.class, args);
    }
}
