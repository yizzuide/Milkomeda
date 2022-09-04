package com.github.yizzuide.milkomeda.demo;

import com.github.yizzuide.milkomeda.universe.extend.env.YmlPropertySourceFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MilkomedaDemoApplication
 *
 * @author yizzuide
 * Create at 2019/03/30 19:04
 */
@EnableMilkomeda
// 导入配置类
@PropertySource(value = "classpath:conf.properties", encoding = "UTF-8")
@PropertySource(value = "classpath:api.yml", name = "api", encoding = "UTF-8", factory = YmlPropertySourceFactory.class)
@MapperScan(basePackages = "com.github.yizzuide.milkomeda.demo.*.mapper")
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
// 开启Servlet组件扫描，如：WebFilter、WebServlet
//@ServletComponentScan
@SpringBootApplication
public class MilkomedaDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(MilkomedaDemoApplication.class, args);
    }
}
