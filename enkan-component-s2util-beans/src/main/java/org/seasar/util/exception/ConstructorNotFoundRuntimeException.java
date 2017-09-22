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

import java.lang.reflect.Constructor;

/**
 * {@link Constructor}が見つからなかったときにスローされる例外Vです。
 *
 * @author higa
 */
public class ConstructorNotFoundRuntimeException extends RuntimeException {
    private final Class<?> targetClass;

    private final Object[] methodArgs;

    private final Class<?>[] paramTypes;

    /**
     * {@link ConstructorNotFoundRuntimeException}を作成します。
     *
     * @param targetClass
     *            ターゲットクラス
     * @param methodArgs
     *            引数の並び
     */
    public ConstructorNotFoundRuntimeException(final Class<?> targetClass,
            final Object[] methodArgs) {
        this.targetClass = targetClass;
        this.methodArgs = methodArgs;
        paramTypes = null;
    }

    /**
     * {@link ConstructorNotFoundRuntimeException}を作成します。
     *
     * @param targetClass
     *            ターゲットクラス
     * @param paramTypes
     *            引数型の並び
     */
    public ConstructorNotFoundRuntimeException(final Class<?> targetClass,
            final Class<?>[] paramTypes) {
        this.targetClass = targetClass;
        this.paramTypes = paramTypes;
        methodArgs = null;
    }

    /**
     * ターゲットクラスを返します。
     *
     * @return ターゲットクラス
     */
    public Class<?> getTargetClass() {
        return targetClass;
    }

    /**
     * 引数の並びを返します。
     *
     * @return 引数の並び
     */
    public Object[] getMethodArgs() {
        return methodArgs;
    }

    /**
     * 引数型の並びを返します。
     *
     * @return 引数型の並び
     */
    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    private static String getSignature(final Object... methodArgs) {
        if (methodArgs == null || methodArgs.length == 0) {
            return "";
        }
        final StringBuilder buf = new StringBuilder(100);
        for (final Object arg : methodArgs) {
            if (arg != null) {
                buf.append(arg.getClass().getName());
            } else {
                buf.append("null");
            }
            buf.append(", ");
        }
        buf.setLength(buf.length() - 2);
        return new String(buf);
    }

    private static String getSignature(final Class<?>... paramTypes) {
        if (paramTypes == null || paramTypes.length == 0) {
            return "";
        }
        final StringBuilder buf = new StringBuilder(100);
        for (final Class<?> type : paramTypes) {
            if (type != null) {
                buf.append(type.getName());
            } else {
                buf.append("null");
            }
            buf.append(", ");
        }
        buf.setLength(buf.length() - 2);
        return buf.toString();
    }

}
