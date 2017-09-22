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
package org.seasar.util.lang;

import java.lang.reflect.*;
import java.util.*;

/**
 * genericsを扱うためのユーティリティ・クラスです。
 *
 * @author koichik
 */
public abstract class GenericsUtil {

    /**
     * <code>type</code>の原型が<code>clazz</code>に代入可能であれば<code>true</code>を、
     * それ以外の場合は<code>false</code>を返します。
     *
     * @param type
     *            タイプ。{@literal null}であってはいけません
     * @param clazz
     *            クラス。{@literal null}であってはいけません
     * @return <code>type</code>の原型が<code>clazz</code>に代入可能であれば<code>true</code>
     */
    public static boolean isTypeOf(final Type type, final Class<?> clazz) {
        if (type == null) throw new IllegalArgumentException("type is null");
        if (clazz == null) throw new IllegalArgumentException("clazz is null");

        if (type instanceof Class) {
            return clazz.isAssignableFrom((Class<?>) type);
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType =
                ParameterizedType.class.cast(type);
            return isTypeOf(parameterizedType.getRawType(), clazz);
        }
        return false;
    }

    /**
     * <code>type</code>の原型を返します。
     * <ul>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            タイプ
     * @return <code>type</code>の原型
     */
    public static Class<?> getRawClass(final Type type) {
        if (type instanceof Class) {
            return Class.class.cast(type);
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType =
                ParameterizedType.class.cast(type);
            return getRawClass(parameterizedType.getRawType());
        }
        if (type instanceof WildcardType) {
            final WildcardType wildcardType = WildcardType.class.cast(type);
            final Type[] types = wildcardType.getUpperBounds();
            return getRawClass(types[0]);
        }
        if (type instanceof GenericArrayType) {
            final GenericArrayType genericArrayType =
                GenericArrayType.class.cast(type);
            final Class<?> rawClass =
                getRawClass(genericArrayType.getGenericComponentType());
            return Array.newInstance(rawClass, 0).getClass();
        }
        return null;
    }

    /**
     * <code>type</code>の型引数の配列を返します。
     * <p>
     * <code>type</code>が配列型の場合はその要素型(それが配列の場合はさらにその要素型)を対象とします。
     * </p>
     * <p>
     * <code>type</code>がパラメータ化された型であっても、直接型引数を持たない場合は空の配列を返します。
     * パラメータ化された型の中にネストされた、型引数を持たない型などがその例です。
     * </p>
     * <p>
     * <code>type</code>がパラメータ化された型でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            タイプ
     * @return <code>type</code>の型引数の配列
     * @see ParameterizedType#getActualTypeArguments()
     */
    public static Type[] getGenericParameters(final Type type) {
        if (type instanceof ParameterizedType) {
            return ParameterizedType.class.cast(type).getActualTypeArguments();
        }
        if (type instanceof GenericArrayType) {
            return getGenericParameters(GenericArrayType.class
                .cast(type)
                .getGenericComponentType());
        }
        return null;
    }

    /**
     * 指定された位置の<code>type</code>の型引数を返します。
     * <p>
     * <code>type</code>がパラメータ化された型でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            タイプ
     * @param index
     *            位置
     * @return 指定された位置の<code>type</code>の型引数
     */
    public static Type getGenericParameter(final Type type, final int index) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        final Type[] genericParameter = getGenericParameters(type);
        if (genericParameter == null) {
            return null;
        }
        return genericParameter[index];
    }

    /**
     * パラメータ化された型を要素とする配列の要素型を返します。
     * <p>
     * <code>type</code>がパラメータ化された型の配列でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            パラメータ化された型を要素とする配列
     * @return パラメータ化された型を要素とする配列の要素型
     */
    public static Type getElementTypeOfArray(final Type type) {
        if (!(type instanceof GenericArrayType)) {
            return null;
        }
        return GenericArrayType.class.cast(type).getGenericComponentType();
    }

    /**
     * パラメータ化された{@link Collection}の要素型を返します。
     * <p>
     * <code>type</code>がパラメータ化された{@link List}でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            パラメータ化された{@link List}
     * @return パラメータ化された{@link List}の要素型
     */
    public static Type getElementTypeOfCollection(final Type type) {
        if (!isTypeOf(type, Collection.class)) {
            return null;
        }
        return getGenericParameter(type, 0);
    }

    /**
     * パラメータ化された{@link List}の要素型を返します。
     * <p>
     * <code>type</code>がパラメータ化された{@link List}でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            パラメータ化された{@link List}
     * @return パラメータ化された{@link List}の要素型
     */
    public static Type getElementTypeOfList(final Type type) {
        if (!isTypeOf(type, List.class)) {
            return null;
        }
        return getGenericParameter(type, 0);
    }

    /**
     * パラメータ化された{@link Set}の要素型を返します。
     * <p>
     * <code>type</code>がパラメータ化された{@link Set}でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            パラメータ化された{@link Set}
     * @return パラメータ化された{@link Set}の要素型
     */
    public static Type getElementTypeOfSet(final Type type) {
        if (!isTypeOf(type, Set.class)) {
            return null;
        }
        return getGenericParameter(type, 0);
    }

    /**
     * パラメータ化された{@link Map}のキーの型を返します。
     * <p>
     * <code>type</code>がパラメータ化された{@link Map}でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            パラメータ化された{@link Map}
     * @return パラメータ化された{@link Map}のキーの型
     */
    public static Type getKeyTypeOfMap(final Type type) {
        if (!isTypeOf(type, Map.class)) {
            return null;
        }
        return getGenericParameter(type, 0);
    }

    /**
     * パラメータ化された{@link Map}の値の型を返します。
     * <p>
     * <code>type</code>がパラメータ化された{@link Map}でない場合は<code>null</code>を返します。
     * </p>
     *
     * @param type
     *            パラメータ化された{@link Map}
     * @return パラメータ化された{@link Map}の値の型
     */
    public static Type getValueTypeOfMap(final Type type) {
        if (!isTypeOf(type, Map.class)) {
            return null;
        }
        return getGenericParameter(type, 1);
    }

    /**
     * パラメータ化された型(クラスまたはインタフェース)が持つ型変数をキー、型引数を値とする{@link Map}を返します。
     *
     * @param clazz
     *            パラメータ化された型(クラスまたはインタフェース)。{@literal null}であってはいけません
     * @return パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     */
    public static Map<TypeVariable<?>, Type> getTypeVariableMap(
            final Class<?> clazz) {
        if (clazz == null) throw new IllegalArgumentException("clazz is null");

        final Map<TypeVariable<?>, Type> map = new LinkedHashMap<>();


        final TypeVariable<?>[] typeParameters = clazz.getTypeParameters();
        for (final TypeVariable<?> typeParameter : typeParameters) {
            map.put(
                typeParameter,
                getActualClass(typeParameter.getBounds()[0], map));
        }

        final Class<?> superClass = clazz.getSuperclass();
        final Type superClassType = clazz.getGenericSuperclass();
        if (superClass != null) {
            gatherTypeVariables(superClass, superClassType, map);
        }

        final Class<?>[] interfaces = clazz.getInterfaces();
        final Type[] interfaceTypes = clazz.getGenericInterfaces();
        for (int i = 0; i < interfaces.length; ++i) {
            gatherTypeVariables(interfaces[i], interfaceTypes[i], map);
        }

        return map;
    }

    /**
     * パラメータ化された型(クラスまたはインタフェース)が持つ型変数および型引数を集めて<code>map</code>に追加します。
     *
     * @param clazz
     *            クラス
     * @param type
     *            型
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     */
    protected static void gatherTypeVariables(final Class<?> clazz,
            final Type type, final Map<TypeVariable<?>, Type> map) {
        if (clazz == null) {
            return;
        }
        gatherTypeVariables(type, map);

        final Class<?> superClass = clazz.getSuperclass();
        final Type superClassType = clazz.getGenericSuperclass();
        if (superClass != null) {
            gatherTypeVariables(superClass, superClassType, map);
        }

        final Class<?>[] interfaces = clazz.getInterfaces();
        final Type[] interfaceTypes = clazz.getGenericInterfaces();
        for (int i = 0; i < interfaces.length; ++i) {
            gatherTypeVariables(interfaces[i], interfaceTypes[i], map);
        }
    }

    /**
     * パラメータ化された型(クラスまたはインタフェース)が持つ型変数および型引数を集めて<code>map</code>に追加します。
     *
     * @param type
     *            型
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     */
    protected static void gatherTypeVariables(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType =
                ParameterizedType.class.cast(type);
            final TypeVariable<?>[] typeVariables =
                GenericDeclaration.class
                    .cast(parameterizedType.getRawType())
                    .getTypeParameters();
            final Type[] actualTypes =
                parameterizedType.getActualTypeArguments();
            for (int i = 0; i < actualTypes.length; ++i) {
                map.put(typeVariables[i], actualTypes[i]);
            }
        }
    }

    /**
     * <code>type</code>の実際の型を返します。
     * <ul>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が型変数で引数{@code map}のキーとして含まれている場合はその変数の実際の型引数を返します。</li>
     * <li><code>type</code>が型変数で引数{@code map}のキーとして含まれていない場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            タイプ
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     * @return <code>type</code>の実際の型
     */
    public static Class<?> getActualClass(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (type instanceof Class) {
            return Class.class.cast(type);
        }
        if (type instanceof ParameterizedType) {
            return getActualClass(ParameterizedType.class
                .cast(type)
                .getRawType(), map);
        }
        if (type instanceof WildcardType) {
            return getActualClass(WildcardType.class
                .cast(type)
                .getUpperBounds()[0], map);
        }
        if (type instanceof TypeVariable) {
            final TypeVariable<?> typeVariable = TypeVariable.class.cast(type);
            if (map.containsKey(typeVariable)) {
                return getActualClass(map.get(typeVariable), map);
            }
            return getActualClass(typeVariable.getBounds()[0], map);
        }
        if (type instanceof GenericArrayType) {
            final GenericArrayType genericArrayType =
                GenericArrayType.class.cast(type);
            final Class<?> componentClass =
                getActualClass(genericArrayType.getGenericComponentType(), map);
            return Array.newInstance(componentClass, 0).getClass();
        }
        return null;
    }

    /**
     * パラメータ化された型を要素とする配列の実際の要素型を返します。
     * <ul>
     * <li><code>type</code>がパラメータ化された型の配列でない場合は<code>null</code>を返します。</li>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が型変数の場合はその変数の実際の型引数を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            パラメータ化された型を要素とする配列
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     * @return パラメータ化された型を要素とする配列の実際の要素型
     */
    public static Class<?> getActualElementClassOfArray(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (!(type instanceof GenericArrayType)) {
            return null;
        }
        return getActualClass(GenericArrayType.class
            .cast(type)
            .getGenericComponentType(), map);
    }

    /**
     * パラメータ化された{@link Collection}の実際の要素型を返します。
     * <ul>
     * <li><code>type</code>がパラメータ化された{@link Collection}でない場合は<code>null</code>
     * を返します。</li>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が型変数の場合はその変数の実際の型引数を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            パラメータ化された{@link Collection}
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     * @return パラメータ化された{@link Collection}の実際の要素型
     */
    public static Class<?> getActualElementClassOfCollection(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (!isTypeOf(type, Collection.class)) {
            return null;
        }
        return getActualClass(getGenericParameter(type, 0), map);
    }

    /**
     * パラメータ化された{@link List}の実際の要素型を返します。
     * <ul>
     * <li><code>type</code>がパラメータ化された{@link List}でない場合は<code>null</code>を返します。</li>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が型変数の場合はその変数の実際の型引数を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            パラメータ化された{@link List}
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     * @return パラメータ化された{@link List}の実際の要素型
     */
    public static Class<?> getActualElementClassOfList(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (!isTypeOf(type, List.class)) {
            return null;
        }
        return getActualClass(getGenericParameter(type, 0), map);
    }

    /**
     * パラメータ化された{@link Set}の実際の要素型を返します。
     * <ul>
     * <li><code>type</code>がパラメータ化された{@link Set}でない場合は<code>null</code>を返します。</li>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が型変数の場合はその変数の実際の型引数を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            パラメータ化された{@link Set}
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     * @return パラメータ化された{@link Set}の実際の要素型
     */
    public static Class<?> getActualElementClassOfSet(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (!isTypeOf(type, Set.class)) {
            return null;
        }
        return getActualClass(getGenericParameter(type, 0), map);
    }

    /**
     * パラメータ化された{@link Map}のキーの実際の型を返します。
     * <ul>
     * <li>キー型がパラメータ化された{@link Map}でない場合は<code>null</code>を返します。</li>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が型変数の場合はその変数の実際の型引数を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            パラメータ化された{@link Map}
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     * @return パラメータ化された{@link Map}のキーの実際の型
     */
    public static Class<?> getActualKeyClassOfMap(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (!isTypeOf(type, Map.class)) {
            return null;
        }
        return getActualClass(getGenericParameter(type, 0), map);
    }

    /**
     * パラメータ化された{@link Map}の値の実際の型を返します。
     * <ul>
     * <li><code>type</code>がパラメータ化された{@link Map}でない場合は<code>null</code>を返します。</li>
     * <li><code>type</code>が<code>Class</code>の場合はそのまま返します。</li>
     * <li><code>type</code>がパラメータ化された型の場合はその原型を返します。</li>
     * <li><code>type</code>がワイルドカード型の場合は(最初の)上限境界を返します。</li>
     * <li><code>type</code>が型変数の場合はその変数の実際の型引数を返します。</li>
     * <li><code>type</code>が配列の場合はその要素の実際の型の配列を返します。</li>
     * <li>その他の場合は<code>null</code>を返します。</li>
     * </ul>
     *
     * @param type
     *            パラメータ化された{@link Map}
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     * @return パラメータ化された{@link Map}の値の実際の型
     */
    public static Class<?> getActualValueClassOfMap(final Type type,
            final Map<TypeVariable<?>, Type> map) {
        if (!isTypeOf(type, Map.class)) {
            return null;
        }
        return getActualClass(getGenericParameter(type, 1), map);
    }

}
