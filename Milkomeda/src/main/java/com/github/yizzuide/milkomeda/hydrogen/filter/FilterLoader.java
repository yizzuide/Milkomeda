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

package com.github.yizzuide.milkomeda.hydrogen.filter;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenLoader;

import javax.servlet.Filter;
import javax.servlet.ServletContext;

/**
 * FilterLoader
 * 过滤器加载器
 *
 * @author yizzuide
 * @since 3.0.0
 * <br>
 * Create at 2020/04/03 01:04
 */
public interface FilterLoader extends HydrogenLoader {

    /**
     * 设置Servlet上下文
     * @param servletContext    ServletContext
     */
    void setServletContext(ServletContext servletContext);

    /**
     * 动态加载一个Filter（仅适用于容器Tomcat)
     * @param name          过滤器名
     * @param clazz         过滤器类
     * @param urlPatterns   匹配路径
     * @return 加载是否成功
     */
    boolean load(String name, Class<? extends Filter> clazz, String... urlPatterns);

    /**
     * 动态删除一个Filter（仅适用于容器Tomcat)
     * @param name  过滤器名
     * @return 删除是否成功
     */
    boolean unload(String name);
}
