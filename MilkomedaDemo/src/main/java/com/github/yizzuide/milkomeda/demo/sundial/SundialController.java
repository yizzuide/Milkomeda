package com.github.yizzuide.milkomeda.demo.sundial;

import com.github.yizzuide.milkomeda.fusion.Fusion;
import com.github.yizzuide.milkomeda.sundial.SundialDynamicDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

/**
 * @date: 2020/5/8
 * @author: jsq
 * @email: 786063250@qq.com
 * @describe:
 */
@Slf4j
@RestController
@RequestMapping("sundial")
public class SundialController {

    @Autowired
    private DataSource dataSource;

    @SundialDynamicDataSource
    @Fusion
    @RequestMapping("get")
    private String getData() {
        log.info("get datasource: {}", dataSource.getClass());
        return "OK";
    }

    @SundialDynamicDataSource(value = "read-only")
    @RequestMapping("slave")
    private String slave() {
        log.info("get datasource: {}", dataSource.getClass());
        return "OK";
    }
}
