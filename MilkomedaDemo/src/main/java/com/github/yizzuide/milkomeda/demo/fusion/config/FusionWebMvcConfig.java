package com.github.yizzuide.milkomeda.demo.fusion.config;

import com.github.yizzuide.milkomeda.demo.fusion.vo.ReturnVO;
import com.github.yizzuide.milkomeda.fusion.FusionAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcConfig
 *
 * @author yizzuide
 * Create at 2019/07/02 11:00
 */
@Configuration
public class FusionWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    public void configFusion(FusionAspect fusionAspect) {
        // 修改返回值
        fusionAspect.setConverter((tag, retObj, error) -> {
            // 返回错误类型响应数据
            if (retObj == null) {
                return new ReturnVO<>().error(error);
            }
            return new ReturnVO<>().ok(retObj);
        });
    }
}
