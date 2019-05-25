package com.github.yizzuide.milkomeda.demo.comet.web.controller;

import com.github.yizzuide.milkomeda.comet.Comet;
import com.github.yizzuide.milkomeda.demo.comet.pojo.ProfileCometData;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * CollectController
 *
 * @author yizzuide
 * Create at 2019/04/11 22:17
 */
@RestController
@RequestMapping("collect")
public class CollectController {

    @Autowired
    private ApplicationContextHolder applicationContextHolder;

    @RequestMapping("feature")
    @Comet(apiCode = "1.1", description = "上传用户特征", tag = "profile", prototype = ProfileCometData.class)
    public ResponseEntity<Map> feature(@RequestParam Map<String, String> params) {
        System.out.println(params);
        System.out.println(applicationContextHolder.getApplicationContext());
        Map<String, String> map = new HashMap<>();
        map.put("code", "200");
        map.put("data", null);
//        throw new RuntimeException("出错了");
        // TODO 注意：这里的返回值会被 CometConfig 里的切面修改掉！
        return ResponseEntity.ok(map);
    }
}
