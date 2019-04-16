package com.github.yizzuide.milkomeda.demo.comet.config;

import com.github.yizzuide.milkomeda.comet.CometAspect;
import com.github.yizzuide.milkomeda.comet.CometData;
import com.github.yizzuide.milkomeda.comet.CometRecorder;
import com.github.yizzuide.milkomeda.demo.comet.pojo.ProfileCometData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

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
                // 根据 prototype 实际采集日志实体，这里可以根据业务添加相应业务
                if (tag.equals("profile")) {
                    ProfileCometData profileCometData = (ProfileCometData) prototype;
                    String uid = String.valueOf(request.getParameter("uid"));
                    profileCometData.setUid(uid);
                }
                log.info("onRequest {} - {} - {}", prototype, tag, request);
            }

            @Override
            public void onReturn(CometData cometData) {
                log.info("onReturn {}", cometData);
            }

            @Override
            public void onThrowing(CometData cometData) {
                log.error("onThrowing {}", cometData);
            }
        });
    }
}
