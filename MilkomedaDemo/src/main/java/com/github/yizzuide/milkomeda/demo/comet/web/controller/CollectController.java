package com.github.yizzuide.milkomeda.demo.comet.web.controller;

import com.github.yizzuide.milkomeda.comet.core.Comet;
import com.github.yizzuide.milkomeda.comet.core.CometParam;
import com.github.yizzuide.milkomeda.demo.comet.pojo.ProfileWebCometData;
import com.github.yizzuide.milkomeda.demo.comet.service.CollectService;
import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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
//        throw new RuntimeException("出错了");
        return ResponseEntity.ok(map);
    }

    @RequestMapping("product/click")
    public void /*Map<String, String>*/ click(@RequestParam("productId") String productId, HttpServletResponse response) throws IOException {
        log.info("用户点击了产品：{}", productId);
        Map<String, String> map = new HashMap<>();
        map.put("code", "1");
        map.put("data", "成功");
//        throw new RuntimeException("出错了");
//        return map;

        try {
            // 设置中间解析的数据
            Map<String, Object> data = new HashMap<>();
            data.put("code", "137");
            data.put("taskId", "123354665656");
            AopContextHolder.getWebCometData().setIntentData(data);
            // 测试异常
            int i = 1 / 0;
        } catch (Exception e) {
            // 标记异常
            AopContextHolder.getWebCometData().setFailure(e);
            response.setStatus(500);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            PrintWriter writer = response.getWriter();
            writer.println("{}");
            writer.flush();
            return;
        }

        response.setStatus(200);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        PrintWriter writer = response.getWriter();
        writer.println(JSONUtil.serialize(map));
        writer.flush();
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
