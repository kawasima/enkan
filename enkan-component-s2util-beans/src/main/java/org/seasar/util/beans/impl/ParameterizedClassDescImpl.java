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
package org.seasar.util.beans.impl;

import org.seasar.util.beans.ParameterizedClassDesc;

/**
 * {@link ParameterizedClassDesc}の実装クラスです。
 *
 * @author koichik
 */
public class ParameterizedClassDescImpl implements ParameterizedClassDesc {

    /** 原型となるクラス */
    protected Class<?> rawClass;

    /** 型引数を表す{@link ParameterizedClassDesc}の配列 */
    protected ParameterizedClassDesc[] arguments;

    /**
     * インスタンスを構築します。
     *
     * @param rawClass
     *            原型となるクラス。{@literal null}であってはいけません
     */
    public ParameterizedClassDescImpl(final Class<?> rawClass) {
        if (rawClass == null) throw new IllegalArgumentException("rawClass is null");

        this.rawClass = rawClass;
    }

    /**
     * インスタンスを構築します。
     *
     * @param rawClass
     *            原型となるクラス。{@literal null}であってはいけません
     * @param arguments
     *            型引数を表す{@link ParameterizedClassDesc}の配列
     */
    public ParameterizedClassDescImpl(final Class<?> rawClass,
            final ParameterizedClassDesc[] arguments) {
        if (rawClass == null) throw new IllegalArgumentException("rawClass is null");

        this.rawClass = rawClass;
        this.arguments = arguments;
    }

    @Override
    public boolean isParameterizedClass() {
        return arguments != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Class<T> getRawClass() {
        return (Class<T>) rawClass;
    }

    /**
     * 原型となるクラスを設定します。
     *
     * @param rawClass
     *            原型となるクラス。{@literal null}であってはいけません
     */
    public void setRawClass(final Class<?> rawClass) {
        if (rawClass == null) throw new IllegalArgumentException("rawClass is null");


        this.rawClass = rawClass;
    }

    @Override
    public ParameterizedClassDesc[] getArguments() {
        return arguments;
    }

    /**
     * 型引数を表す{@link ParameterizedClassDesc}の配列を設定します。
     *
     * @param arguments
     *            型引数を表す{@link ParameterizedClassDesc}の配列
     */
    public void setArguments(final ParameterizedClassDesc[] arguments) {
        this.arguments = arguments;
    }

}
