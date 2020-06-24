package com.github.yizzuide.milkomeda.particle;

import com.github.yizzuide.milkomeda.universe.algorithm.hash.BloomHashWrapper;
import com.github.yizzuide.milkomeda.util.RedisUtil;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * BloomLimiter
 * 布隆限制器
 *
 * @author yizzuide
 * @since 3.9.0
 * Create at 2020/06/23 16:03
 */
public class BloomLimiter extends LimitHandler {
    /**
     * 配置存储bitmap的key
     */
    private String bitKey;
    /**
     * 键值分隔器（limit的key必需包含过滤的值，且通过该分隔器能获取）
     */
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
    private String valueSeparator = "_";

    /**
     * 记录数据量
     */
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
    private int insertions = 2 << 24; // 33554432 ~= 12M

    /**
     * 误差率
     */
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
    private double fpp = 0.03;

    /**
     * Bloom Hash操作类
     */
    private volatile BloomHashWrapper<String> bloomHashWrapper;

    public <R> R limit(String key, Process<R> process) throws Throwable {
        return limit(key, -1, process);
    }

    @Override
    public <R> R limit(String key, long expire, Process<R> process) throws Throwable {
        int splitIndex = key.lastIndexOf(valueSeparator);
        if (StringUtils.isEmpty(bitKey)) {
            bitKey = key.substring(0, splitIndex);
        }
        String value = key.substring(splitIndex + 1);
        int[] offset = getBloomHashWrapper().offset(value);
        Particle particle = null;
        for (int i : offset) {
            Boolean hit = getRedisTemplate().execute((RedisConnection connection) -> connection.getBit(bitKey.getBytes(), i));
            assert hit != null;
            // 只要有一bit为0，即匹配失败
            if (!hit) {
                particle = new Particle(this.getClass(), true, 0);
                break;
            }
        }
        if (particle == null) {
            particle = new Particle(this.getClass(), false, 1);
        }
        return next(particle, key, expire, process);
    }

    /**
     * 添加数据到bitmap
     * @param value 记录值
     */
    public void add(String value) {
        addAll(Collections.singletonList(value));
    }

    /**
     * 添加数据到bitmap
     * @param values 记录列表
     */
    public void addAll(List<String> values) {
        assert bitKey != null;
        RedisUtil.batchConn((connection) -> {
            for (String value : values) {
                int[] offset = getBloomHashWrapper().offset(value);
                for (int i : offset) {
                    connection.setBit(bitKey.getBytes(), i, true);
                }
            }
        }, getRedisTemplate());
    }

    private BloomHashWrapper<String> getBloomHashWrapper() {
        // 双重检测
        if (bloomHashWrapper == null) {
            synchronized (this) {
                if (bloomHashWrapper == null) {
                    bloomHashWrapper = new BloomHashWrapper<>(insertions, fpp);
                }
            }
        }
        return bloomHashWrapper;
    }
}
