package com.github.yizzuide.milkomeda.demo.pillar.pillars;

import com.github.yizzuide.milkomeda.demo.pillar.common.ReturnData;
import com.github.yizzuide.milkomeda.demo.pillar.common.TradeType;
import com.github.yizzuide.milkomeda.pillar.Pillar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * WithDrawPillar
 *
 * @author yizzuide
 * <br />
 * Create at 2019/06/28 18:43
 */
@Slf4j
@Component
public class WithDrawPillar implements Pillar<Map<String, String>, ReturnData> {
    @Override
    public String supportType() {
        return TradeType.WITHDRAW.pillarType();
    }

    @Override
    public void process(Map<String, String> params, ReturnData result) {
        log.info("正在组装提现的参数：{}", params);
        log.info("调用第三方支付平台来完成提现");
        // 解析第三方支付平台的返回结果。。
        // 响应处理结果
        result.setCode("200");
        result.setSuccess(true);
    }
}
