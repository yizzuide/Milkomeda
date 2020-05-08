package com.github.yizzuide.milkomeda.demo.sundial;

import com.github.yizzuide.milkomeda.sundial.SundialDynamicDataSource;
import com.github.yizzuide.milkomeda.sundial.DataSourceType;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @date: 2020/5/8
 * @author: jsq
 * @email: 786063250@qq.com
 * @describe:
 */
@Configuration
public class SundialController {


    @SundialDynamicDataSource(DataSourceType.MASTER )
    @RequestMapping("111")
    private void test(){
        System.out.println(111);
    }
}
