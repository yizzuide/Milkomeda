package com.github.yizzuide.milkomeda.demo.comet.config;

import com.github.yizzuide.milkomeda.comet.CometAspect;
import com.github.yizzuide.milkomeda.comet.CometData;
import com.github.yizzuide.milkomeda.comet.CometRecorder;
import com.github.yizzuide.milkomeda.comet.WebCometData;
import com.github.yizzuide.milkomeda.demo.comet.pojo.ProfileWebCometData;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * CometConfig
 *
 * @author yizzuide
 * Create at 2019/04/11 22:18
 */
@Slf4j
@Configuration
public class CometConfig {

    @Autowired
    public void config(CometAspect cometAspect) {
        // 设置日记采集器
        cometAspect.setRecorder(new CometRecorder() {
            @Override
            public void onRequest(CometData prototype, String tag, HttpServletRequest request, Object[] args) {
                log.info("onRequest {} - {} - {} - {}", prototype, tag, request, args);
                // 根据 prototype 实际采集日志实体，这里可以根据业务添加相应业务
                if (tag.equals("profile")) {
                    ProfileWebCometData profileCometData = (ProfileWebCometData) prototype;
                    String uid = String.valueOf(request.getParameter("uid"));
                    profileCometData.setUid(uid);
                    // 模拟一个日志实体
                    Map<String, Object> log = new HashMap<>();
                    log.put("uid", profileCometData.getUid());
                    profileCometData.setAttachment(log);
                }
                // 异步将日志存储到MySQL数据库或ES
            }

            @Override
            public Object onReturn(CometData cometData, Object returnData) {
                log.info("onReturn {}", cometData);

                // 异步将日志存储到MySQL数据库或ES

                // 这里可以修改返回值
                if (cometData instanceof WebCometData && returnData instanceof ResponseEntity) {
                    return ResponseEntity.ok("ok");
                }
                return returnData;
            }

            @Override
            public void onThrowing(CometData cometData, Exception e) {
                log.error("onThrowing {}", JSONUtil.serialize(cometData));
                // 查看日志实体
                log.info("attachment: {}", cometData.getAttachment());
            }
        });
    }
}
