/*
 * Copyright (c) 2023 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.universe.extend.loader;

import com.github.yizzuide.milkomeda.util.IOUtils;

import java.io.IOException;

/**
 * The loader Specify loading lua file.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/05/19 03:20
 */
public interface LuaLoader {
    /**
     * Lua script path.
     */
    default String resourcePath() {
        return IOUtils.LUA_PATH;
    }

    /**
     * Lua script file name.
     * @return file name
     */
    String filename();

    /**
     * Setter of hold content filed.
     */
    void setLuaScript(String luaScript);

    /**
     * Getter of hold content filed.
     * @return script content
     */
    String getLuaScript();

    /**
     * Start load lua script.
     */
    default void load() throws IOException {
        String luaScript = IOUtils.loadLua(resourcePath(), filename());
        setLuaScript(luaScript);
    }
}
