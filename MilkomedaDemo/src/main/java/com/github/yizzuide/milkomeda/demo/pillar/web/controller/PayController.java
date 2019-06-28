package com.github.yizzuide.milkomeda.demo.pillar.web.controller;

import com.github.yizzuide.milkomeda.demo.pillar.common.ReturnData;
import com.github.yizzuide.milkomeda.demo.pillar.common.TradeType;
import com.github.yizzuide.milkomeda.demo.pillar.pojo.PayEntity;
import com.github.yizzuide.milkomeda.demo.pillar.service.PayService;
import com.github.yizzuide.milkomeda.light.LightCache;
import com.github.yizzuide.milkomeda.light.Spot;
import com.github.yizzuide.milkomeda.pillar.Pillar;
import com.github.yizzuide.milkomeda.pillar.PillarExecutor;
import com.github.yizzuide.milkomeda.pillar.PillarRecognizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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

    @Autowired
    private PillarExecutor<Map<String, String>, ReturnData> pillarExecutor;

    // 模拟一个服务对象
    @Resource
    private PayService payService;

    @Resource
    private LightCache<Pillar<Map<String, String>, ReturnData>, PayEntity> lightCache;

    /**
     * 第三方平台银行卡预支付
     * @param params type 支付类型
     * @return ReturnData
     */
    @RequestMapping("bankcardPrepay")
    public ResponseEntity<ReturnData> bankcardPrepay(@RequestParam Map<String, String> params) {
        String type = params.get("type");
        // 匹配标识符
        String pillarType = PillarRecognizer.typeOf(TradeType.values(), type);

        Spot<Pillar<Map<String, String>, ReturnData>, PayEntity> spot = lightCache.get(type);
        if (spot == null) {
            Pillar<Map<String, String>, ReturnData> pillar = pillarExecutor.getPillars(pillarType).get(0);
            // 模拟从数据库查找
            PayEntity payEntity = payService.get(type);
            spot = new Spot<>(pillar, payEntity);
            lightCache.set(type, spot);
        }
        System.out.println(spot);

        // 直接获得 Pillar 处理单元柱类型名
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
