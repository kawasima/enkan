/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.util.beans.factory;

import org.seasar.util.beans.BeanDesc;
import org.seasar.util.beans.impl.BeanDescImpl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link BeanDesc}を生成するクラスです。
 * <p>
 * 指定されたJavaBeansのメタデータを扱う{@link BeanDesc}を返します。
 * </p>
 *
 * <pre>
 * BeanDesc beanDesc = BeanDescFactory.getBeanDesc(Foo.class);
 * </pre>
 * <p>
 * {@link BeanDesc}はキャッシュされます。
 * </p>
 *
 * @author higa
 * @see BeanDesc
 */
public abstract class BeanDescFactory {

    /** {@literal true} if {@link BeanDescFactory} was initialized. */
    private static volatile boolean initialized;

    /** The cache of {@link BeanDesc}. */
    private static final ConcurrentMap<Class<?>, BeanDesc> beanDescCache =
        new ConcurrentHashMap<>(1024);

    static {
        initialize();
    }

    /**
     * {@link BeanDesc}を返します。
     *
     * @param clazz
     *            Beanクラス。{@literal null}であってはいけません
     * @return {@link BeanDesc}
     */
    public static BeanDesc getBeanDesc(final Class<?> clazz) {
        if (clazz == null) throw new IllegalArgumentException("clazz is null");

        if (!initialized) {
            initialize();
        }
        BeanDesc beanDesc = beanDescCache.get(clazz);
        if (beanDesc == null) {
            beanDesc = beanDescCache.computeIfAbsent(clazz, clz -> new BeanDescImpl(clz));
        }
        return beanDesc;
    }

    /**
     * 初期化を行ないます。
     */
    public static void initialize() {
        synchronized (BeanDescFactory.class) {
            if (!initialized) {
                initialized = true;
            }
        }
    }

    /**
     * キャッシュをクリアします。
     */
    public static void clear() {
        beanDescCache.clear();
        initialized = false;
    }

}
