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
package org.seasar.util.convert;

/**
 * {@link Boolean}用の変換ユーティリティです。
 *
 * @author higa
 */
public abstract class BooleanConversionUtil {

    /**
     * {@link Boolean}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @return 変換された{@link Boolean}
     */
    public static Boolean toBoolean(final Object o) {
        switch (o) {
            case null -> {
                return null;
            }
            case Boolean b -> {
                return b;
            }
            case Number number -> {
                final int num = number.intValue();
                return num != 0;
            }
            case String s -> {
                if ("true".equalsIgnoreCase(s)) {
                    return Boolean.TRUE;
                } else if ("false".equalsIgnoreCase(s)) {
                    return Boolean.FALSE;
                } else if (s.equals("0")) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            }
            default -> {
                return Boolean.TRUE;
            }
        }
    }

    /**
     * {@literal boolean}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @return 変換された{@literal boolean}
     */
    public static boolean toPrimitiveBoolean(final Object o) {
        final Boolean b = toBoolean(o);
        return b != null && b;
    }

}
