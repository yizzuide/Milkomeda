package com.github.yizzuide.milkomeda.demo.comet.web.controller;

import com.github.yizzuide.milkomeda.comet.core.Comet;
import com.github.yizzuide.milkomeda.comet.core.CometParam;
import com.github.yizzuide.milkomeda.demo.comet.pojo.ProfileWebCometData;
import com.github.yizzuide.milkomeda.demo.comet.service.CollectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * CollectController
 *
 * @author yizzuide
 * Create at 2019/04/11 22:17
 */
@Slf4j
@RestController
@RequestMapping("collect")
public class CollectController {

    @Resource
    private CollectService collectService;

    @RequestMapping("feature")
    @Comet(apiCode = "1.1", name = "上传用户特征", tag = "PROFILE", prototype = ProfileWebCometData.class)
    public ResponseEntity<Map<String, String>> feature(@RequestParam Map<String, String> params) {
        collectService.save(1, params);
        Map<String, String> map = new HashMap<>();
        map.put("code", "200");
        map.put("data", null);
        throw new RuntimeException("出错了");
//        return ResponseEntity.ok(map);
    }

    @RequestMapping("product/click")
    public ResponseEntity<Map<String, String>> click(@RequestParam("productId") String productId) {
        log.info("用户点击了产品：{}", productId);
        Map<String, String> map = new HashMap<>();
        map.put("code", "200");
        map.put("data", null);
        throw new RuntimeException("出错了");
//        return ResponseEntity.ok(map);
    }

    @PostMapping("usage")
    // @CometParam注解提供支持接收form表单数据、JSON数据（特别适用于不知道第三方回调的方式下）
    public ResponseEntity<String> usage(@CometParam Map<String, String> params) {
        log.info("请求参数：{}", params);
        return ResponseEntity.ok("ok");
    }

    // 测试无参数读body的问题
    @GetMapping("ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
