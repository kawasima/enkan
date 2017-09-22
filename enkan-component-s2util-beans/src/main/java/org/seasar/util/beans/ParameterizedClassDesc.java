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
package org.seasar.util.beans;

import java.lang.reflect.ParameterizedType;

/**
 * パラメタ化されたクラスを扱うためのインターフェースです。
 * 
 * @author koichik
 */
public interface ParameterizedClassDesc {

    /**
     * このインスタンスが表現するクラスがパラメタ化されていれば<code>true</code>を返します。
     * 
     * @return このインスタンスが表現するクラスがパラメタ化されていれば<code>true</code>
     */
    boolean isParameterizedClass();

    /**
     * 原型となるクラスを返します。
     * 
     * @param <T>
     *            原型となるクラスの型
     * @return 原型となるクラス
     * @see ParameterizedType#getRawType()
     */
    <T> Class<T> getRawClass();

    /**
     * 型引数を表す{@link ParameterizedClassDesc}の配列を返します。
     * <p>
     * このインスタンスが表現するクラスがパラメタ化されたクラスでない場合は、{@literal null}を返します。
     * </p>
     * 
     * @return 型引数を表す{@link ParameterizedClassDesc}の配列
     * @see ParameterizedType#getActualTypeArguments()
     */
    ParameterizedClassDesc[] getArguments();

}
