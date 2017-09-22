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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * メソッドを扱うためのインターフェースです。
 * 
 * @author koichik
 */
public interface MethodDesc {

    /**
     * このメソッドを所有するクラスの{@link BeanDesc}を返します。
     * 
     * @return {@link BeanDesc}
     */
    BeanDesc getBeanDesc();

    /**
     * メソッドを返します。
     * 
     * @return コンストラクタ
     */
    Method getMethod();

    /**
     * メソッド名を返します。
     * 
     * @return メソッド名
     */
    String getMethodName();

    /**
     * メソッドの引数型の配列を返します。
     * 
     * @return メソッドの引数型の配列
     */
    Class<?>[] getParameterTypes();

    /**
     * メソッドの戻り値の型を返します。
     * 
     * @param <T>
     *            メソッドの戻り値の型
     * @return メソッドの戻り値の型
     */
    <T> Class<T> getReturnType();

    /**
     * {@literal public}メソッドの場合は{@literal true}を返します。
     * 
     * @return {@literal public}メソッドの場合は{@literal true}
     */
    boolean isPublic();

    /**
     * {@literal static}メソッドの場合は{@literal true}を返します。
     * 
     * @return {@literal static}メソッドの場合は{@literal true}
     */
    boolean isStatic();

    /**
     * {@literal final}メソッドの場合は{@literal true}を返します。
     * 
     * @return {@literal final}メソッドの場合は{@literal true}
     */
    boolean isFinal();

    /**
     * {@literal abstract}メソッドの場合は{@literal true}を返します。
     * 
     * @return {@literal abstract}メソッドの場合は{@literal true}
     */
    boolean isAbstract();

    /**
     * メソッドの引数型がパラメタ化された型の場合は{@literal true}を返します。
     * 
     * @param index
     *            引数のインデックス
     * @return 引数型がパラメタ化された型の場合は{@literal true}
     */
    boolean isParameterized(int index);

    /**
     * 戻り値型がパラメタ化された型の場合は{@literal true}を返します。
     * 
     * @return 戻り値型がパラメタ化された型の場合は{@literal true}
     */
    boolean isParameterized();

    /**
     * メソッドの引数型を表現する{@link ParameterizedClassDesc}の配列を返します。
     * 
     * @return メソッドの引数型を表現する{@link ParameterizedClassDesc}の配列
     */
    ParameterizedClassDesc[] getParameterizedClassDescs();

    /**
     * メソッドの戻り値型を表現する{@link ParameterizedClassDesc}を返します。
     * 
     * @return メソッドの戻り値型を表現する{@link ParameterizedClassDesc}
     */
    ParameterizedClassDesc getParameterizedClassDesc();

    /**
     * メソッドの引数型がパラメタ化された{@link Collection}の場合、その要素型を返します。
     * 
     * @param index
     *            引数のインデックス
     * @return メソッドの引数型がパラメタ化された{@link Collection}の場合はその要素型、そうでない場合は
     *         {@literal null}
     */
    Class<?> getElementClassOfCollection(int index);

    /**
     * メソッドの引数型がパラメタ化された{@link Map}の場合、そのキー型を返します。
     * 
     * @param index
     *            引数のインデックス
     * @return メソッドの引数がパラメタ化された{@link Map}の場合はそのキー型、そうでない場合は{@literal null}
     */
    Class<?> getKeyClassOfMap(int index);

    /**
     * メソッドの引数型がパラメタ化された{@link Map}の場合、その値型を返します。
     * 
     * @param index
     *            引数のインデックス
     * @return メソッドの引数型がパラメタ化された{@link Map}の場合はその値型、そうでない場合は{@literal null}
     */
    Class<?> getValueClassOfMap(int index);

    /**
     * メソッドの戻り値型がパラメタ化された{@link Collection}の場合、その要素型を返します。
     * 
     * @return メソッドの戻り値型がパラメタ化された{@link Collection}の場合はその要素型、そうでない場合は
     *         {@literal null}
     */
    Class<?> getElementClassOfCollection();

    /**
     * メソッドの戻り値型がパラメタ化された{@link Map}の場合、そのキー型を返します。
     * 
     * @return メソッドの戻り値型がパラメタ化された{@link Map}の場合はそのキー型、そうでない場合は{@literal null}
     */
    Class<?> getKeyClassOfMap();

    /**
     * メソッドの戻り値型がパラメタ化された{@link Map}の場合、その値型を返します。
     * 
     * @return メソッドの戻り値型がパラメタ化された{@link Map}の場合はその値型、そうでない場合は{@literal null}
     */
    Class<?> getValueClassOfMap();

    /**
     * メソッドを呼び出してその戻り値を返します。
     * 
     * @param <T>
     *            メソッドの戻り値の型
     * @param target
     *            対象のオブジェクト。{@literal null}であってはいけません
     * @param args
     *            メソッドの引数
     * @return メソッドの戻り値
     */
    <T> T invoke(Object target, Object... args);

    /**
     * staticなメソッドを呼び出してその戻り値を返します。
     * 
     * @param <T>
     *            メソッドの戻り値の型
     * @param args
     *            メソッドの引数
     * @return メソッドの戻り値
     */
    <T> T invokeStatic(Object... args);

}
