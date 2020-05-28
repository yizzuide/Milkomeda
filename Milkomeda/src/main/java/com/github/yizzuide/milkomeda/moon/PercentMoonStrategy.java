package com.github.yizzuide.milkomeda.moon;

import com.github.yizzuide.milkomeda.util.IOUtils;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PercentMoonStrategy
 * 百分比分配 <br>
 *
 * 全部阶段总百分比为100，阶段赋值如下：<br>
 * 33% 设置 33：阶段值为索引0
 * 67% 设置 67：阶段值为索引1
 *
 * @author yizzuide
 * @since 2.6.0
 * @version 2.7.5
 * Create at 2020/03/13 21:42
 */
@Data
public class PercentMoonStrategy extends AbstractLuaMoonStrategy {
    /**
     * 默认总占百分
     */
    public static final int DEFAULT_PERCENT = 100;
    /**
     * 百分比
     */
    private int percent = DEFAULT_PERCENT;

    @Override
    public <T> T getCurrentPhase(Moon<T> moon) {
        throw new UnsupportedOperationException("PercentMoonStrategy is not support run on standalone.");
    }

    @SuppressWarnings("all")
    @Override
    public <T> T getPhase(String key, Integer p, Moon<T> prototype) {
        for (Integer i = 0; i < prototype.getLen(); i++) {
            T phaseName = prototype.getPhaseNames().get(i);
            if (!(phaseName instanceof Integer)) {
                throw new UnsupportedOperationException("Only support int data type.");
            }
            Integer phase = (Integer) phaseName;
            // 累计百分比，使各阶段分配相应百分比的值
            if (i != 0) {
                phase += (Integer) prototype.getPhaseNames().get(i - 1);
            }
            // 找到不满足百分比的阶段，返回阶段索引值
            if (p < phase) {
                return (T) Integer.valueOf(i);
            }
        }
        return null;
    }

    @Override
    public LeftHandPointer pluck(Moon<?> moon, LeftHandPointer leftHandPointer) {
        int p = leftHandPointer.getCurrent();
        p = (p + 1) % this.getPercent();
        leftHandPointer.setCurrent(p);
        return leftHandPointer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getPhaseFast(String key, Moon<T> prototype) {
        RedisTemplate<String, Serializable> redisTemplate = getJsonRedisTemplate();
        RedisScript<Long> redisScript = new DefaultRedisScript<>(getLuaScript(), Long.class);
        Map<String, Integer> phaseMap = new HashMap<>();
        List<T> phaseNames = prototype.getPhaseNames();
        for (int i = 0; i < phaseNames.size(); i++) {
            phaseMap.put("p" + (i + 1), (Integer)phaseNames.get(i));
        }
        Long phase = redisTemplate.execute(redisScript, Collections.singletonList("moon:lhp-" + key), phaseMap, percent);
        assert phase != null;
        return (T) ((Integer)phase.intValue());
    }

    @Override
    public String loadLuaScript() throws IOException {
        return IOUtils.loadLua("/META-INF/scripts", "moon_percent.lua");
    }

    /**
     * 百分比表达式解析 <br>
     * <pre>
     * 百分总量为10时：5/5、3/7、1/5/4、0/10、10/0
     * 百分总量为100时：25/75、10/20/70、0/100、100/0
     * </pre>
     * @param percentExpress    百分比表达式
     * @return 百分比列表
     */
    public static Integer[] parse(String percentExpress) {
        String[] percentComps = percentExpress.split("/");
        int len = percentComps.length;
        if (len < 2) {
            throw new IllegalArgumentException("Percent express format is illegal: " + percentExpress);
        }
        Integer[] percentArray = new Integer[len];
        for (int i = 0; i < len; i++) {
            percentArray[i] = Integer.valueOf(percentComps[i]);
        }
        return percentArray;
    }
}
