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
 * {@link Double}用の変換ユーティリティです。
 *
 * @author higa
 */
public abstract class DoubleConversionUtil {

    /**
     * {@link Double}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @return 変換された{@link Double}
     */
    public static Double toDouble(final Object o) {
        return toDouble(o, null);
    }

    /**
     * {@link Double}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@link Double}
     */
    public static Double toDouble(final Object o, final String pattern) {
        switch (o) {
            case null -> {
                return null;
            }
            case Double v -> {
                return v;
            }
            case Number number -> {
                return number.doubleValue();
            }
            case String s -> {
                return toDouble(s);
            }
            case java.util.Date date -> {
                if (pattern != null) {
                    return Double.parseDouble(new SimpleDateFormat(pattern).format(o));
                }
                return (double) date.getTime();
            }
            default -> {
                return toDouble(o.toString());
            }
        }
    }

    private static Double toDouble(final String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return Double.parseDouble(s);
    }

    /**
     * {@literal double}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @return 変換された{@literal double}
     */
    public static double toPrimitiveDouble(final Object o) {
        return toPrimitiveDouble(o, null);
    }

    /**
     * {@literal double}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@literal double}
     */
    public static double toPrimitiveDouble(final Object o, final String pattern) {
        switch (o) {
            case null -> {
                return 0;
            }
            case Number number -> {
                return number.doubleValue();
            }
            case String s -> {
                return toPrimitiveDouble(s);
            }
            case java.util.Date date -> {
                if (pattern != null) {
                    return Double.parseDouble(new SimpleDateFormat(pattern)
                            .format(o));
                }
                return date.getTime();
            }
            default -> {
                return toPrimitiveDouble(o.toString());
            }
        }
    }

    private static double toPrimitiveDouble(final String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        return Double.parseDouble(s);
    }

}
