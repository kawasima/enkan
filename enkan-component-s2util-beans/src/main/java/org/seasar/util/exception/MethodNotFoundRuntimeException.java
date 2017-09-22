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
package org.seasar.util.exception;

import java.lang.reflect.Method;

/**
 * {@link Method}が見つからなかったときにスローされる例外です。
 *
 * @author higa
 *
 */
public class MethodNotFoundRuntimeException extends RuntimeException {
    private final Class<?> targetClass;

    private final String methodName;

    private final Class<?>[] methodArgClasses;

    /**
     * {@link MethodNotFoundRuntimeException}を作成します。
     *
     * @param targetClass
     *            ターゲットクラス
     * @param methodName
     *            メソッド名
     * @param methodArgs
     *            引数の並び
     */
    public MethodNotFoundRuntimeException(final Class<?> targetClass,
            final String methodName, final Object[] methodArgs) {
        this(targetClass, methodName, toClassArray(methodArgs));
    }

    /**
     * {@link MethodNotFoundRuntimeException}を作成します。
     *
     * @param targetClass
     *            ターゲットクラス
     * @param methodName
     *            メソッド名
     * @param methodArgClasses
     *            引数型の並び
     */
    public MethodNotFoundRuntimeException(final Class<?> targetClass,
            final String methodName, final Class<?>[] methodArgClasses) {
        this.targetClass = targetClass;
        this.methodName = methodName;
        this.methodArgClasses = methodArgClasses;
    }

    /**
     * ターゲットの{@link Class}を返します。
     *
     * @return ターゲットの{@link Class}
     */
    public Class<?> getTargetClass() {
        return targetClass;
    }

    /**
     * メソッド名を返します。
     *
     * @return メソッド名
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * メソッドの引数の{@link Class}の配列を返します。
     *
     * @return メソッドの引数の{@link Class}の配列
     */
    public Class<?>[] getMethodArgClasses() {
        return methodArgClasses;
    }

    private static Class<?>[] toClassArray(final Object... methodArgs) {
        if (methodArgs == null) {
            return null;
        }
        final Class<?>[] result = new Class[methodArgs.length];
        for (int i = 0; i < methodArgs.length; ++i) {
            if (methodArgs[i] != null) {
                result[i] = methodArgs[i].getClass();
            }
        }
        return result;
    }

}
