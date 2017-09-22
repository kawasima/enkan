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

/**
 * プロパティが見つからなかった場合にスローされる例外です。
 *
 * @author higa
 *
 */
public class PropertyNotFoundRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -5177019197796206774L;

    private final Class<?> targetClass;

    private final String propertyName;

    /**
     * {@link PropertyNotFoundRuntimeException}を返します。
     *
     * @param targetClass
     *            ターゲットクラス
     * @param propertyName
     *            プロパティ名
     */
    public PropertyNotFoundRuntimeException(final Class<?> targetClass,
            final String propertyName) {
        this.targetClass = targetClass;
        this.propertyName = propertyName;
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
     * プロパティ名を返します。
     *
     * @return プロパティ名
     */
    public String getPropertyName() {
        return propertyName;
    }

}
