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
import org.seasar.util.beans.FieldDesc;
import org.seasar.util.beans.ParameterizedClassDesc;
import org.seasar.util.beans.factory.ParameterizedClassDescFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

import static enkan.util.ReflectionUtils.tryReflection;

/**
 * {@link FieldDesc}の実装クラスです。
 *
 * @author koichik
 */
public class FieldDescImpl implements FieldDesc {

    /** このフィールドを所有するクラスの{@link BeanDesc} */
    protected final BeanDesc beanDesc;

    /** フィールド */
    protected final Field field;

    /** フィールド名 */
    protected final String fieldName;

    /** フィールドの型 */
    protected final Class<?> fieldType;

    /** パラメータ化された型の情報 */
    protected final ParameterizedClassDesc parameterizedClassDesc;

    /**
     * インスタンスを構築します。
     *
     * @param beanDesc
     *            このフィールドを所有するクラスの{@link BeanDesc}。{@literal null}であってはいけません
     * @param field
     *            フィールド。{@literal null}であってはいけません
     */
    public FieldDescImpl(final BeanDesc beanDesc, final Field field) {
        if (beanDesc == null) throw new IllegalArgumentException("beanDesc is null");
        if (field    == null) throw new IllegalArgumentException("field is null");

        this.beanDesc = beanDesc;
        this.field = field;
        fieldName = field.getName();
        fieldType = field.getType();
        parameterizedClassDesc =
            ParameterizedClassDescFactory.createParameterizedClassDesc(
                field,
                beanDesc.getTypeVariables());
    }

    @Override
    public BeanDesc getBeanDesc() {
        return beanDesc;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Class<T> getFieldType() {
        return (Class<T>) fieldType;
    }

    @Override
    public boolean isPublic() {
        return Modifier.isPublic(field.getModifiers());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(field.getModifiers());
    }

    @Override
    public boolean isParameterized() {
        return parameterizedClassDesc != null
            && parameterizedClassDesc.isParameterizedClass();
    }

    @Override
    public ParameterizedClassDesc getParameterizedClassDesc() {
        return parameterizedClassDesc;
    }

    @Override
    public Class<?> getElementClassOfCollection() {
        if (!Collection.class.isAssignableFrom(fieldType) || !isParameterized()) {
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
        if (!Map.class.isAssignableFrom(fieldType) || !isParameterized()) {
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
        if (!Map.class.isAssignableFrom(fieldType) || !isParameterized()) {
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
    public <T> T getFieldValue(final Object target) {
        if (target == null) throw new IllegalArgumentException("target is null");

        return (T) tryReflection(() -> field.get(target));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getStaticFieldValue() {
        return (T) tryReflection(() -> field.get(null));
    }

    @Override
    public void setFieldValue(final Object target, final Object value) {
        if (target == null) throw new IllegalArgumentException("target is null");

        tryReflection(() -> {
            field.set(target, value);
            return null;
        });
    }

    @Override
    public void setStaticFieldValue(final Object value) {
        tryReflection(() -> {
            field.set(null, value);
            return null;
        });
    }

}
