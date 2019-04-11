package com.github.yizzuide.milkomeda.demo.pillar.pillars;

import com.github.yizzuide.milkomeda.demo.pillar.common.ReturnData;
import com.github.yizzuide.milkomeda.demo.pillar.common.TradeType;
import com.github.yizzuide.milkomeda.pillar.Pillar;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * PayPillar
 * 支付单元柱分支
 *
 * @author yizzuide
 * Create at 2019/04/11 16:53
 */
@Slf4j
public class PayPillar implements Pillar<Map<String, String>, ReturnData> {
    @Override
    public String supportType() {
        return TradeType.PAY.getTypeName();
    }

    @Override
    public void process(Map<String, String> params, ReturnData result) {
      log.info("正在组装支付的参数：{}", params);
      log.info("调用第三方支付平台来完成支付");
        // 解析第三方支付平台的返回结果。。
        // 响应处理结果
        result.setCode("200");
        result.setSuccess(true);
    }
}
