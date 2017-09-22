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
        if (o == null) {
            return null;
        } else if (o instanceof Short) {
            return (Short) o;
        } else if (o instanceof Number) {
            return Short.valueOf(((Number) o).shortValue());
        } else if (o instanceof String) {
            return toShort((String) o);
        } else if (o instanceof java.util.Date) {
            if (pattern != null) {
                return Short.valueOf(new SimpleDateFormat(pattern).format(o));
            }
            return Short.valueOf((short) ((java.util.Date) o).getTime());
        } else if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue() ? Short.valueOf((short) 1)
                : Short.valueOf((short) 0);
        } else {
            return toShort(o.toString());
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
        if (o == null) {
            return 0;
        } else if (o instanceof Number) {
            return ((Number) o).shortValue();
        } else if (o instanceof String) {
            return toPrimitiveShort((String) o);
        } else if (o instanceof java.util.Date) {
            if (pattern != null) {
                return Short
                    .parseShort(new SimpleDateFormat(pattern).format(o));
            }
            return (short) ((java.util.Date) o).getTime();
        } else if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue() ? (short) 1 : (short) 0;
        } else {
            return toPrimitiveShort(o.toString());
        }
    }

    private static short toPrimitiveShort(final String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        return Short.parseShort(s);
    }

}
