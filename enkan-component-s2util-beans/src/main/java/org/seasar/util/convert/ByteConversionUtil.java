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
 * {@link Byte}用の変換ユーティリティです。
 *
 * @author higa
 */
public abstract class ByteConversionUtil {

    /**
     * {@link Byte}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @return 変換された{@link Byte}
     */
    public static Byte toByte(final Object o) {
        return toByte(o, null);
    }

    /**
     * {@link Byte}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@link Byte}
     */
    public static Byte toByte(final Object o, final String pattern) {
        if (o == null) {
            return null;
        } else if (o instanceof Byte) {
            return (Byte) o;
        } else if (o instanceof Number) {
            return ((Number) o).byteValue();
        } else if (o instanceof String) {
            return toByte((String) o);
        } else if (o instanceof java.util.Date) {
            if (pattern != null) {
                return Byte.valueOf(new SimpleDateFormat(pattern).format(o));
            }
            return (byte) ((java.util.Date) o).getTime();
        } else if (o instanceof Boolean) {
            return (Boolean) o ? Byte.valueOf((byte) 1) : Byte
                .valueOf((byte) 0);
        } else {
            return toByte(o.toString());
        }
    }

    private static Byte toByte(final String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return Byte.valueOf(s);
    }

    /**
     * {@literal byte}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @return 変換された{@literal byte}
     */
    public static byte toPrimitiveByte(final Object o) {
        return toPrimitiveByte(o, null);
    }

    /**
     * {@literal byte}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@literal byte}
     */
    public static byte toPrimitiveByte(final Object o, final String pattern) {
        if (o == null) {
            return 0;
        } else if (o instanceof Number) {
            return ((Number) o).byteValue();
        } else if (o instanceof String) {
            return toPrimitiveByte((String) o);
        } else if (o instanceof java.util.Date) {
            if (pattern != null) {
                return Byte.parseByte(new SimpleDateFormat(pattern).format(o));
            }
            return (byte) ((java.util.Date) o).getTime();
        } else if (o instanceof Boolean) {
            return (Boolean) o ? (byte) 1 : (byte) 0;
        } else {
            return toPrimitiveByte(o.toString());
        }
    }

    private static byte toPrimitiveByte(final String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        return Byte.parseByte(s);
    }

}
