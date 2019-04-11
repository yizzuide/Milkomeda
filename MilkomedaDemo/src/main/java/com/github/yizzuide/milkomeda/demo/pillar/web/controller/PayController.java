package com.github.yizzuide.milkomeda.demo.pillar.web.controller;

import com.github.yizzuide.milkomeda.demo.pillar.pillars.PayPillar;
import com.github.yizzuide.milkomeda.demo.pillar.pillars.RechargePillar;
import com.github.yizzuide.milkomeda.demo.pillar.common.ReturnData;
import com.github.yizzuide.milkomeda.demo.pillar.common.TradeType;
import com.github.yizzuide.milkomeda.pillar.PillarExecutor;
import com.github.yizzuide.milkomeda.pillar.PillarRecognizer;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

/**
 * PayController
 *
 * @author yizzuide
 * Create at 2019/04/11 16:50
 */
@RestController
@RequestMapping("pay")
public class PayController {

    private PillarExecutor<Map<String, String>, ReturnData> pillarExecutor;
    {
        pillarExecutor = new PillarExecutor<>();
        pillarExecutor.addPillarList(Arrays.asList(new PayPillar(), new RechargePillar()));
    }

    /**
     * 第三方平台银行卡预支付
     * @param params type 支付类型
     * @return ReturnData
     */
    @RequestMapping("bankcardPrepay")
    public ResponseEntity<ReturnData> bankcardPrepay(@RequestParam Map<String, String> params) throws Exception {
        Integer type = Integer.valueOf(params.get("type"));
        // 通过此方法可以直接拿到枚举
//        TradeType tradeType = PillarRecognizer.typeOfEnum(TradeType.class, type);
        // 直接获得 Pillar 处理单元柱类型名
        val pillarType = PillarRecognizer.typeOf(TradeType.class, type);
        ReturnData returnData = new ReturnData();
        if (pillarType == null) {
            returnData.setCode("400");
            returnData.setSuccess(false);
            returnData.setMsg("type 参数错误");
            return ResponseEntity.ok(returnData);
        }
        // 将 if/else 分支分流
        pillarExecutor.execute(pillarType, params, returnData);
        return ResponseEntity.ok(returnData);
    }
    
}
