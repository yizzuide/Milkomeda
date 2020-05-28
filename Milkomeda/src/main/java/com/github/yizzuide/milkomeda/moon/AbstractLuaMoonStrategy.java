package com.github.yizzuide.milkomeda.moon;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.io.Serializable;

/**
 * AbstractLuaMoonStrategy
 *
 * @author yizzuide
 * @since 3.7.0
 * Create at 2020/05/28 23:59
 */
public abstract class AbstractLuaMoonStrategy implements MoonStrategy {

    private String luaScript;

    private RedisTemplate<String, Serializable> jsonRedisTemplate;

    @SuppressWarnings("unchecked")
    protected RedisTemplate<String, Serializable> getJsonRedisTemplate() {
        if (jsonRedisTemplate == null) {
            jsonRedisTemplate = ApplicationContextHolder.get().getBean("jsonRedisTemplate", RedisTemplate.class);
        }
        return jsonRedisTemplate;
    }

    /**
     * 加载lua脚本
     * @return  lua脚本
     * @throws IOException  读取异常
     */
    public abstract String loadLuaScript() throws IOException;

    public void setLuaScript(String luaScript) {
        this.luaScript = luaScript;
    }

    public String getLuaScript() {
        return this.luaScript;
    }
}
