package com.github.yizzuide.milkomeda.universe.ognl;

import ognl.DefaultClassResolver;

/**
 * OgnlClassResolver
 *
 * @author yizzuide
 * @since 3.5.0
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
