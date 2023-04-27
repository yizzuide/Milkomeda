package com.github.yizzuide.milkomeda.demo.metal;

import com.github.yizzuide.milkomeda.metal.Metal;
import com.github.yizzuide.milkomeda.metal.MetalChangeEvent;
import com.github.yizzuide.milkomeda.metal.MetalHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MetalController
 *
 * @author yizzuide
 * <br>
 * Create at 2020/05/21 23:41
 */
@Slf4j
@RequestMapping("metal")
@RestController
public class MetalController {

    @Metal("platform")
    private String platform;

    // 支持配置名与字段名相同
    @Metal
    private Double version;

    // 支持自动类型转换
    @Metal
//    private Map<String, BigDecimal> feeRateConfig;
    private FeeRateConfig feeRateConfig;

    @RequestMapping("get")
    public String get() {
        log.info("feeRateConfig: {}", feeRateConfig);
        return String.format("platform: %s, version:%f", platform, version);
    }

    @RequestMapping("platform/update")
    public String update(String name) {
        // 更新数据库里配置...
        // 更新本地缓存配置
//        MetalHolder.updateProperty("platform", name);
        // 更新分布式服务配置
        MetalHolder.remoteUpdateProperty("platform", name);
        return "OK";
    }

    // 监听配置的修改
    @EventListener(condition = "event.key == 'platform'")
    public void handlePlatformChange(MetalChangeEvent event) {
        log.info("handlePlatformChange: {}", event);
    }
}
