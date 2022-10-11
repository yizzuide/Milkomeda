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

package com.github.yizzuide.milkomeda.universe.engine.ognl;

import ognl.DefaultClassResolver;

/**
 * OgnlClassResolver
 *
 * @author yizzuide
 * @since 3.5.0
 * <br>
 * Create at 2020/05/20 13:56
 */
public class OgnlClassResolver extends DefaultClassResolver {

    @SuppressWarnings("rawtypes")
    @Override
    protected Class toClassForName(String className) throws ClassNotFoundException {
        return classForName(className, getClassLoaders(null));
    }

    private Class<?> classForName(String name, ClassLoader[] classLoader) throws ClassNotFoundException {
        for (ClassLoader cl : classLoader) {
            if (null != cl) {
                return Class.forName(name, true, cl);
            }
        }
        throw new ClassNotFoundException("Cannot find class: " + name);
    }

    private ClassLoader[] getClassLoaders(ClassLoader classLoader) {
        return new ClassLoader[] {
                classLoader,
                Thread.currentThread().getContextClassLoader(),
                getClass().getClassLoader(),
                ClassLoader.getSystemClassLoader()
        };
    }
}
