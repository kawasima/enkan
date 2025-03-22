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
        switch (o) {
            case null -> {
                return null;
            }
            case Byte b -> {
                return b;
            }
            case Number number -> {
                return number.byteValue();
            }
            case String s -> {
                return toByte(s);
            }
            case java.util.Date date -> {
                if (pattern != null) {
                    return Byte.valueOf(new SimpleDateFormat(pattern).format(o));
                }
                return (byte) date.getTime();
            }
            case Boolean b -> {
                return b ? Byte.valueOf((byte) 1) : Byte
                        .valueOf((byte) 0);
            }
            default -> {
                return toByte(o.toString());
            }
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
        switch (o) {
            case null -> {
                return 0;
            }
            case Number number -> {
                return number.byteValue();
            }
            case String s -> {
                return toPrimitiveByte(s);
            }
            case java.util.Date date -> {
                if (pattern != null) {
                    return Byte.parseByte(new SimpleDateFormat(pattern).format(o));
                }
                return (byte) date.getTime();
            }
            case Boolean b -> {
                return b ? (byte) 1 : (byte) 0;
            }
            default -> {
                return toPrimitiveByte(o.toString());
            }
        }
    }

    private static byte toPrimitiveByte(final String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        return Byte.parseByte(s);
    }

}
