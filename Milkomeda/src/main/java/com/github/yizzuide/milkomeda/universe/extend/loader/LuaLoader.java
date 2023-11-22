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
     * Lua script dir path.
     * @return lua source dir.
     */
    default String resourceDirPath() {
        return IOUtils.LUA_PATH;
    }

    /**
     * Lua script file name list.
     * @return list file name
     */
    String[] luaFilenames();

    /**
     * Setter of hold content filed.
     * @param luaScripts lua script file list
     */
    void setLuaScripts(String[] luaScripts);

    /**
     * Start load lua script.
     * @throws IOException if load file not exists.
     */
    default void load() throws IOException {
        String[] luaFilenames = luaFilenames();
        if (luaFilenames == null || luaFilenames.length == 0) {
            return;
        }
        String[] luaScripts = new String[luaFilenames.length];
        for (int i = 0; i < luaFilenames.length; i++) {
            luaScripts[i] = IOUtils.loadLua(resourceDirPath(), luaFilenames[i]);
        }
        setLuaScripts(luaScripts);
    }
}
