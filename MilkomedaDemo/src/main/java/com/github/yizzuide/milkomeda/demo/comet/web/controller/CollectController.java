package com.github.yizzuide.milkomeda.demo.comet.web.controller;

import com.github.yizzuide.milkomeda.comet.Comet;
import com.github.yizzuide.milkomeda.demo.comet.pojo.ProfileWebCometData;
import com.github.yizzuide.milkomeda.demo.comet.service.CollectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.github.yizzuide.milkomeda.demo.comet.collector.CollectorType.TAG_PROFILE;

/**
 * CollectController
 *
 * @author yizzuide
 * Create at 2019/04/11 22:17
 */
@RestController
@RequestMapping("collect")
public class CollectController {

    @Resource
    private CollectService collectService;

    @RequestMapping("feature")
    @Comet(apiCode = "1.1", name = "上传用户特征", tag = TAG_PROFILE, prototype = ProfileWebCometData.class)
    public ResponseEntity<Map<String, String>> feature(@RequestParam Map<String, String> params, HttpServletRequest request) {
        System.out.println(request.getHeader("accept-language"));
        System.out.println(params);
        collectService.save(1, params);
        Map<String, String> map = new HashMap<>();
        map.put("code", "200");
        map.put("data", null);
//        throw new RuntimeException("出错了");
        // 注意：这里的返回值会被 CometConfig 里的切面修改掉！
        return ResponseEntity.ok(map);
    }
}
