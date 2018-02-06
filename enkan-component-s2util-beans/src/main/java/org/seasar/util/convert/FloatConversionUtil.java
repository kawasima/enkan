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
 * {@link Float}用の変換ユーティリティです。
 *
 * @author higa
 */
public abstract class FloatConversionUtil {

    /**
     * {@link Float}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @return 変換された{@link Float}
     */
    public static Float toFloat(final Object o) {
        return toFloat(o, null);
    }

    /**
     * {@link Float}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@link Float}
     */
    public static Float toFloat(final Object o, final String pattern) {
        if (o == null) {
            return null;
        } else if (o instanceof Float) {
            return (Float) o;
        } else if (o instanceof Number) {
            return ((Number) o).floatValue();
        } else if (o instanceof String) {
            return toFloat((String) o);
        } else if (o instanceof java.util.Date) {
            if (pattern != null) {
                return Float.parseFloat(new SimpleDateFormat(pattern).format(o));
            }
            return (float) ((java.util.Date) o).getTime();
        } else {
            return toFloat(o.toString());
        }
    }

    private static Float toFloat(final String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return Float.parseFloat(s);
    }

    /**
     * {@literal float}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @return 変換された{@literal float}
     */
    public static float toPrimitiveFloat(final Object o) {
        return toPrimitiveFloat(o, null);
    }

    /**
     * {@literal float}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@literal float}
     */
    public static float toPrimitiveFloat(final Object o, final String pattern) {
        if (o == null) {
            return 0;
        } else if (o instanceof Number) {
            return ((Number) o).floatValue();
        } else if (o instanceof String) {
            return toPrimitiveFloat((String) o);
        } else if (o instanceof java.util.Date) {
            if (pattern != null) {
                return Float
                    .parseFloat(new SimpleDateFormat(pattern).format(o));
            }
            return ((java.util.Date) o).getTime();
        } else {
            return toPrimitiveFloat(o.toString());
        }
    }

    private static float toPrimitiveFloat(final String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        return Float.parseFloat(s);
    }

}
