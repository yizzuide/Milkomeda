/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
