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

import org.seasar.util.beans.*;
import org.seasar.util.beans.impl.ParameterizedClassDescImpl;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * フィールの型やメソッドの引数型、戻り値型を表現する{@link ParameterizedClassDesc}を作成するファクトリです。
 * <p>
 * このクラスでは{@link ParameterizedClassDesc}のインスタンスをキャッシュしません。 {@link BeanDesc}経由で
 * {@link ParameterizedClassDesc}を取得するようにしてください。
 * </p>
 *
 * @author koichik
 * @see BeanDesc#getTypeVariables()
 * @see PropertyDesc#getParameterizedClassDesc()
 * @see FieldDesc#getParameterizedClassDesc()
 * @see ConstructorDesc#getParameterizedClassDescs()
 * @see MethodDesc#getParameterizedClassDesc()
 * @see MethodDesc#getParameterizedClassDescs()
 */
public abstract class ParameterizedClassDescFactory {

    /**
     * パラメータ化された型(クラスまたはインタフェース)が持つ型変数をキー、型引数を値とする{@link Map}を返します。
     * <p>
     * 型がパラメタ化されていない場合は空の{@link Map}を返します。
     * </p>
     *
     * @param beanClass
     *            パラメータ化された型(クラスまたはインタフェース)。{@literal null}であってはいけません
     * @return パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     */
    public static Map<TypeVariable<?>, Type> getTypeVariables(
            final Class<?> beanClass) {
        if (beanClass == null) throw new IllegalArgumentException("beanClass is null");

        return getTypeVariableMap(beanClass);
    }

    /**
     * フィールドの型を表現する{@link ParameterizedClassDesc}を作成して返します。
     *
     * @param field
     *            フィールド。{@literal null}であってはいけません
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}。{@literal null}
     *            であってはいけません
     * @return フィールドの型を表現する{@link ParameterizedClassDesc}
     */
    public static ParameterizedClassDesc createParameterizedClassDesc(
            final Field field, final Map<TypeVariable<?>, Type> map) {
        if (field == null) throw new IllegalArgumentException("field is null");
        if (map == null) throw new IllegalArgumentException("map is null");

        return createParameterizedClassDesc(field.getGenericType(), map);
    }

    /**
     * コンストラクタの引数型を表現する{@link ParameterizedClassDesc}を作成して返します。
     *
     * @param constructor
     *            コンストラクタ。{@literal null}であってはいけません
     * @param index
     *            引数の位置
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}。{@literal null}
     *            であってはいけません
     * @return メソッドの引数型を表現する{@link ParameterizedClassDesc}
     */
    public static ParameterizedClassDesc createParameterizedClassDesc(
            final Constructor<?> constructor, final int index,
            final Map<TypeVariable<?>, Type> map) {
        if (constructor == null) throw new IllegalArgumentException("constructor is null");
        if (map == null) throw new IllegalArgumentException("map is null");

        return createParameterizedClassDesc(
            constructor.getGenericParameterTypes()[index],
            map);
    }

    /**
     * メソッドの引数型を表現する{@link ParameterizedClassDesc}を作成して返します。
     *
     * @param method
     *            メソッド。{@literal null}であってはいけません
     * @param index
     *            引数の位置
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}。{@literal null}
     *            であってはいけません
     * @return メソッドの引数型を表現する{@link ParameterizedClassDesc}
     */
    public static ParameterizedClassDesc createParameterizedClassDesc(
            final Method method, final int index,
            final Map<TypeVariable<?>, Type> map) {
        if (method == null) throw new IllegalArgumentException("method is null");
        if (method.getParameterTypes().length <= index)
            throw new IllegalArgumentException("index");
        if (map == null) throw new IllegalArgumentException("map is null");

        return createParameterizedClassDesc(
            method.getGenericParameterTypes()[index],
            map);
    }

    /**
     * メソッドの戻り値型を表現する{@link ParameterizedClassDesc}を作成して返します。
     *
     * @param method
     *            メソッド。{@literal null}であってはいけません
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}。{@literal null}
     *            であってはいけません
     * @return メソッドの戻り値型を表現する{@link ParameterizedClassDesc}
     */
    public static ParameterizedClassDesc createParameterizedClassDesc(
            final Method method, final Map<TypeVariable<?>, Type> map) {
        if (method == null) throw new IllegalArgumentException("method is null");
        if (map    == null) throw new IllegalArgumentException("map is null");

        return createParameterizedClassDesc(method.getGenericReturnType(), map);
    }

    /**
     * {@link Type}を表現する{@link ParameterizedClassDesc}を作成して返します。
     *
     * @param type
     *            型
     * @param map
     *            パラメータ化された型が持つ型変数をキー、型引数を値とする{@link Map}
     * @return 型を表現する{@link ParameterizedClassDesc}
     */
    protected static ParameterizedClassDesc createParameterizedClassDesc(
            final Type type, final Map<TypeVariable<?>, Type> map) {
        final Class<?> rowClass = getActualClass(type, map);
        if (rowClass == null) {
            return null;
        }
        final ParameterizedClassDescImpl desc =
            new ParameterizedClassDescImpl(rowClass);
        final Type[] parameterTypes = getGenericParameters(type);
        if (parameterTypes == null) {
            return desc;
        }
        final ParameterizedClassDesc[] parameterDescs =
                Arrays.stream(parameterTypes)
                        .map(t -> createParameterizedClassDesc(t, map))
                        .collect(Collectors.toList())
                        .toArray(new ParameterizedClassDesc[parameterTypes.length]);
        desc.setArguments(parameterDescs);
        return desc;
    }

    private static Class<?> getActualClass(final Type type,
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

    private static Type[] getGenericParameters(final Type type) {
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

    private static Map<TypeVariable<?>, Type> getTypeVariableMap(
            final Class<?> clazz) {
        if (clazz == null) throw new IllegalArgumentException("clazz is null");

        final Map<TypeVariable<?>, Type> map =
                new LinkedHashMap<>();

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
}
