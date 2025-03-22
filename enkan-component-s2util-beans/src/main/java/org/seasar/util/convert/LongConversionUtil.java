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
 * {@link Long}用の変換ユーティリティです。
 *
 * @author higa
 */
public abstract class LongConversionUtil {

    /**
     * {@link Long}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @return 変換された{@link Long}
     */
    public static Long toLong(final Object o) {
        return toLong(o, null);
    }

    /**
     * {@link Long}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@link Long}
     */
    public static Long toLong(final Object o, final String pattern) {
        switch (o) {
            case null -> {
                return null;
            }
            case Long l -> {
                return l;
            }
            case Number number -> {
                return number.longValue();
            }
            case String s -> {
                return toLong(s);
            }
            case java.util.Date date -> {
                if (pattern != null) {
                    return Long.valueOf(new SimpleDateFormat(pattern).format(o));
                }
                return date.getTime();
            }
            case Boolean b -> {
                return b ? Long.valueOf(1) : Long
                        .valueOf(0);
            }
            default -> {
                return toLong(o.toString());
            }
        }
    }

    private static Long toLong(final String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return Long.valueOf(s);
    }

    /**
     * 変換された{@literal long}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @return 変換された{@literal long}
     */
    public static long toPrimitiveLong(final Object o) {
        return toPrimitiveLong(o, null);
    }

    /**
     * {@literal long}に変換します。
     *
     * @param o
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@literal long}
     */
    public static long toPrimitiveLong(final Object o, final String pattern) {
        switch (o) {
            case null -> {
                return 0;
            }
            case Number number -> {
                return number.longValue();
            }
            case String s -> {
                return toPrimitiveLong(s);
            }
            case java.util.Date date -> {
                if (pattern != null) {
                    return Long.parseLong(new SimpleDateFormat(pattern).format(o));
                }
                return date.getTime();
            }
            case Boolean b -> {
                return b ? 1 : 0;
            }
            default -> {
                return toPrimitiveLong(o.toString());
            }
        }
    }

    private static long toPrimitiveLong(final String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        return Long.parseLong(s);
    }

}
