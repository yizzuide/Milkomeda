package com.github.yizzuide.milkomeda.demo.comet.config;

import com.github.yizzuide.milkomeda.comet.CometAspect;
import com.github.yizzuide.milkomeda.comet.CometData;
import com.github.yizzuide.milkomeda.comet.CometRecorder;
import com.github.yizzuide.milkomeda.demo.comet.pojo.ProfileCometData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

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
        cometAspect.setRecorder(new CometRecorder() {
            @Override
            public void onRequest(CometData prototype, String tag, HttpServletRequest request) {
                log.info("onRequest {} - {} - {}", prototype, tag, request);
                // 根据 prototype 实际采集日志实体，这里可以根据业务添加相应业务
                if (tag.equals("profile")) {
                    ProfileCometData profileCometData = (ProfileCometData) prototype;
                    String uid = String.valueOf(request.getParameter("uid"));
                    profileCometData.setUid(uid);
                }
            }

            @Override
            public Object onReturn(CometData cometData, Object returnData) {
                log.info("onReturn {}", cometData);
                // 修改返回值!!!
                if (returnData instanceof ResponseEntity) {
                    return ResponseEntity.ok("ok");
                }
                return returnData;
            }

            @Override
            public void onThrowing(CometData cometData, Exception e) {
                log.error("onThrowing {}", cometData);
            }
        });
    }
}
