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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

/**
 * フィールドを扱うためのインターフェースです。
 * 
 * @author koichik
 */
public interface FieldDesc {

    /**
     * このフィールドを所有するクラスの{@link BeanDesc}を返します。
     * 
     * @return {@link BeanDesc}
     */
    BeanDesc getBeanDesc();

    /**
     * フィールドを返します。
     * 
     * @return フィールド
     */
    Field getField();

    /**
     * フィールド名を返します。
     * 
     * @return フィールド名
     */
    String getFieldName();

    /**
     * フィールドの型を返します。
     * 
     * @param <T>
     *            フィールドの型
     * @return フィールドの型
     */
    <T> Class<T> getFieldType();

    /**
     * {@literal public}フィールドの場合は{@literal true}を返します。
     * 
     * @return {@literal public}フィールドの場合は{@literal true}
     */
    boolean isPublic();

    /**
     * {@literal static}フィールドの場合は{@literal true}を返します。
     * 
     * @return {@literal static}フィールドの場合は{@literal true}
     */
    boolean isStatic();

    /**
     * {@literal final}フィールドの場合は{@literal true}を返します。
     * 
     * @return {@literal final}フィールドの場合は{@literal true}
     */
    boolean isFinal();

    /**
     * このフィールドがパラメタ化された型の場合は{@literal true}を返します。
     * 
     * @return このフィールドがパラメタ化された型の場合は{@literal true}
     */
    boolean isParameterized();

    /**
     * フィールドの型を表現する{@link ParameterizedClassDesc}を返します。
     * 
     * @return フィールドの型を表現する{@link ParameterizedClassDesc}
     */
    ParameterizedClassDesc getParameterizedClassDesc();

    /**
     * このフィールドがパラメタ化された{@link Collection}の場合、その要素型を返します。
     * 
     * @return このフィールドがパラメタ化された{@link Collection}の場合はその要素型、そうでない場合は
     *         {@literal null}
     */
    Class<?> getElementClassOfCollection();

    /**
     * このフィールドがパラメタ化された{@link Map}の場合、そのキー型を返します。
     * 
     * @return このフィールドがパラメタ化された{@link Map}の場合はそのキー型、そうでない場合は{@literal null}
     */
    Class<?> getKeyClassOfMap();

    /**
     * このフィールドがパラメタ化された{@link Map}の場合、その値型を返します。
     * 
     * @return このフィールドがパラメタ化された{@link Map}の場合はその値型、そうでない場合は{@literal null}
     */
    Class<?> getValueClassOfMap();

    /**
     * {@link Field}の値を返します。
     * 
     * @param <T>
     *            フィールドの型
     * @param target
     *            対象のオブジェクト。{@literal null}であってはいけません
     * @return {@link Field}の値
     */
    <T> T getFieldValue(Object target);

    /**
     * staticな{@link Field}の値を返します。
     * 
     * @param <T>
     *            フィールドの型
     * @return {@link Field}の値
     */
    <T> T getStaticFieldValue();

    /**
     * {@link Field}の値を設定します。
     * 
     * @param target
     *            対象のオブジェクト。{@literal null}であってはいけません
     * @param value
     *            {@link Field}の値
     */
    void setFieldValue(Object target, Object value);

    /**
     * staticな{@link Field}の値を設定します。
     * 
     * @param value
     *            {@link Field}の値
     */
    void setStaticFieldValue(Object value);

}
