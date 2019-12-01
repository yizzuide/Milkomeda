package com.github.yizzuide.milkomeda.demo.comet.collector;

import com.github.yizzuide.milkomeda.comet.CometData;
import com.github.yizzuide.milkomeda.demo.comet.pojo.ProfileWebCometData;
import com.github.yizzuide.milkomeda.mix.collector.Collector;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * CometCollector
 *
 * @author yizzuide
 * Create at 2019/11/14 16:02
 */
@Component
public class CometCollector implements Collector {

    @Override
    public void prepare(CometData params) {
        ProfileWebCometData profileCometData = (ProfileWebCometData) params;
        String uid = String.valueOf(WebContext.getRequest().getParameter("uid"));
        profileCometData.setUid(uid);
        // 模拟一个日志实体，实际情况应该是一个Model类
        Map<String, Object> log = new HashMap<>();
        log.put("uid", profileCometData.getUid());
        profileCometData.setAttachment(log);
    }

    @Override
    public void onSuccess(CometData params) {
        // 获取实体附件
//        Map<String, Object> apiLog = (Map) params.getAttachment();
//        if (apiLog == null) return;
        // 异步将日志存储到MySQL数据库或ES

    }

    @Override
    public void onFailure(CometData params) {
        // 获取实体附件
//        Map<String, Object> apiLog = (Map) params.getAttachment();
//        if (apiLog == null) return;
        // 异步将日志存储到MySQL数据库或ES
    }

    @Override
    public String supportType() {
        // 指定当前分流类型
        return CollectorType.COLLECTOR_PROFILE.pillarType();
    }
}
