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

import java.text.SimpleDateFormat;

/**
 * {@link Short}用の変換ユーティリティです。
 *
 * @author higa
 */
public abstract class ShortConversionUtil {

    /**
     * {@link Short}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @return 変換された{@link Short}
     */
    public static Short toShort(final Object o) {
        return toShort(o, null);
    }

    /**
     * {@link Short}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@link Short}
     */
    public static Short toShort(final Object o, final String pattern) {
        switch (o) {
            case null -> {
                return null;
            }
            case Short i -> {
                return i;
            }
            case Number number -> {
                return number.shortValue();
            }
            case String s -> {
                return toShort(s);
            }
            case java.util.Date date -> {
                if (pattern != null) {
                    return Short.valueOf(new SimpleDateFormat(pattern).format(o));
                }
                return (short) date.getTime();
            }
            case Boolean b -> {
                return b ? Short.valueOf((short) 1)
                        : Short.valueOf((short) 0);
            }
            default -> {
                return toShort(o.toString());
            }
        }
    }

    private static Short toShort(final String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return Short.valueOf(s);
    }

    /**
     * {@literal short}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @return 変換された{@literal short}
     */
    public static short toPrimitiveShort(final Object o) {
        return toPrimitiveShort(o, null);
    }

    /**
     * {@literal short}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@literal short}
     */
    public static short toPrimitiveShort(final Object o, final String pattern) {
        switch (o) {
            case null -> {
                return 0;
            }
            case Number number -> {
                return number.shortValue();
            }
            case String s -> {
                return toPrimitiveShort(s);
            }
            case java.util.Date date -> {
                if (pattern != null) {
                    return Short
                            .parseShort(new SimpleDateFormat(pattern).format(o));
                }
                return (short) date.getTime();
            }
            case Boolean b -> {
                return b ? (short) 1 : (short) 0;
            }
            default -> {
                return toPrimitiveShort(o.toString());
            }
        }
    }

    private static short toPrimitiveShort(final String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        return Short.parseShort(s);
    }

}
