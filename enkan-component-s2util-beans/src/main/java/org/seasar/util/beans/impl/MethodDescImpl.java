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

import org.seasar.util.beans.BeanDesc;
import org.seasar.util.beans.MethodDesc;
import org.seasar.util.beans.ParameterizedClassDesc;
import org.seasar.util.beans.factory.ParameterizedClassDescFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

import static enkan.util.ReflectionUtils.tryReflection;

/**
 * {@link MethodDesc}の実装クラスです。
 *
 * @author koichik
 */
public class MethodDescImpl implements MethodDesc {

    /** このメソッドを所有するクラスの{@link BeanDesc} */
    protected final BeanDesc beanDesc;

    /** メソッド */
    protected final Method method;

    /** メソッド名 */
    protected final String methodName;

    /** メソッドの引数型の配列 */
    protected final Class<?>[] parameterTypes;

    /** メソッドの戻り値型 */
    protected final Class<?> returnType;

    /** パラメータ化された引数型の情報 */
    protected final ParameterizedClassDesc[] parameterizedClassDescs;

    /** パラメータ化された戻り値型の情報 */
    protected final ParameterizedClassDesc parameterizedClassDesc;

    /**
     * インスタンスを構築します。
     *
     * @param beanDesc
     *            このメソッドを所有するクラスの{@link BeanDesc}。{@literal null}であってはいけません
     * @param method
     *            メソッド。{@literal null}であってはいけません
     */
    public MethodDescImpl(final BeanDesc beanDesc, final Method method) {
        if (beanDesc == null) throw new IllegalArgumentException("beanDesc is null");
        if (method   == null) throw new IllegalArgumentException("method is null");

        this.beanDesc = beanDesc;
        this.method = method;
        methodName = method.getName();
        parameterTypes = method.getParameterTypes();
        returnType = method.getReturnType();
        parameterizedClassDescs =
            new ParameterizedClassDesc[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            parameterizedClassDescs[i] =
                ParameterizedClassDescFactory.createParameterizedClassDesc(
                    method,
                    i,
                    beanDesc.getTypeVariables());
        }
        parameterizedClassDesc =
            ParameterizedClassDescFactory.createParameterizedClassDesc(
                method,
                beanDesc.getTypeVariables());
    }

    @Override
    public BeanDesc getBeanDesc() {
        return beanDesc;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Class<T> getReturnType() {
        return (Class<T>) returnType;
    }

    @Override
    public boolean isPublic() {
        return Modifier.isPublic(method.getModifiers());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(method.getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(method.getModifiers());
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(method.getModifiers());
    }

    @Override
    public boolean isParameterized(final int index) {
        if (parameterTypes.length <= index) throw new IllegalArgumentException("index");

        return parameterizedClassDescs[index].isParameterizedClass();
    }

    @Override
    public boolean isParameterized() {
        return parameterizedClassDesc.isParameterizedClass();
    }

    @Override
    public ParameterizedClassDesc[] getParameterizedClassDescs() {
        return parameterizedClassDescs;
    }

    @Override
    public ParameterizedClassDesc getParameterizedClassDesc() {
        return parameterizedClassDesc;
    }

    @Override
    public Class<?> getElementClassOfCollection(final int index) {
        if (parameterTypes.length <= index) throw new IllegalArgumentException("index");

        if (!Collection.class.isAssignableFrom(parameterTypes[index])
            || !isParameterized(index)) {
            return null;
        }
        final ParameterizedClassDesc pcd =
            parameterizedClassDescs[index].getArguments()[0];
        if (pcd == null) {
            return null;
        }
        return pcd.getRawClass();
    }

    @Override
    public Class<?> getKeyClassOfMap(final int index) {
        if (parameterTypes.length <= index) throw new IllegalArgumentException("index");

        if (!Map.class.isAssignableFrom(parameterTypes[index])
            || !isParameterized(index)) {
            return null;
        }
        final ParameterizedClassDesc pcd =
            parameterizedClassDescs[index].getArguments()[0];
        if (pcd == null) {
            return null;
        }
        return pcd.getRawClass();
    }

    @Override
    public Class<?> getValueClassOfMap(final int index) {
        if (parameterTypes.length <= index) throw new IllegalArgumentException("index");

        if (!Map.class.isAssignableFrom(parameterTypes[index])
            || !isParameterized(index)) {
            return null;
        }
        final ParameterizedClassDesc pcd =
            parameterizedClassDescs[index].getArguments()[1];
        if (pcd == null) {
            return null;
        }
        return pcd.getRawClass();
    }

    @Override
    public Class<?> getElementClassOfCollection() {
        if (!Collection.class.isAssignableFrom(returnType)
            || !isParameterized()) {
            return null;
        }
        final ParameterizedClassDesc pcd =
            parameterizedClassDesc.getArguments()[0];
        if (pcd == null) {
            return null;
        }
        return pcd.getRawClass();
    }

    @Override
    public Class<?> getKeyClassOfMap() {
        if (!Map.class.isAssignableFrom(returnType) || !isParameterized()) {
            return null;
        }
        final ParameterizedClassDesc pcd =
            parameterizedClassDesc.getArguments()[0];
        if (pcd == null) {
            return null;
        }
        return pcd.getRawClass();
    }

    @Override
    public Class<?> getValueClassOfMap() {
        if (!Map.class.isAssignableFrom(returnType) || !isParameterized()) {
            return null;
        }
        final ParameterizedClassDesc pcd =
            parameterizedClassDesc.getArguments()[1];
        if (pcd == null) {
            return null;
        }
        return pcd.getRawClass();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T invoke(final Object target, final Object... args) {
        if (target == null) throw new IllegalArgumentException("target is null");

        return (T) tryReflection(() -> method.invoke(target, args));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T invokeStatic(final Object... args) {
        return (T) tryReflection(() -> method.invoke(null, args));
    }
}
