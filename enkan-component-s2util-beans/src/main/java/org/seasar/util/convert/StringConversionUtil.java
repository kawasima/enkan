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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;

/**
 * {@link String}用の変換ユーティリティです。
 *
 * @author higa
 */
public abstract class StringConversionUtil {

    /** WAVE DASH */
    public static final char WAVE_DASH = '\u301C';

    /** FULLWIDTH TILDE */
    public static final char FULLWIDTH_TILDE = '\uFF5E';

    /** DOUBLE VERTICAL LINE */
    public static final char DOUBLE_VERTICAL_LINE = '\u2016';

    /** PARALLEL TO */
    public static final char PARALLEL_TO = '\u2225';

    /** MINUS SIGN */
    public static final char MINUS_SIGN = '\u2212';

    /** FULLWIDTH HYPHEN-MINUS */
    public static final char FULLWIDTH_HYPHEN_MINUS = '\uFF0D';

    /** CENT SIGN */
    public static final char CENT_SIGN = '\u00A2';

    /** FULLWIDTH CENT SIGN */
    public static final char FULLWIDTH_CENT_SIGN = '\uFFE0';

    /** POUND SIGN */
    public static final char POUND_SIGN = '\u00A3';

    /** FULLWIDTH POUND SIGN */
    public static final char FULLWIDTH_POUND_SIGN = '\uFFE1';

    /** NOT SIGN */
    public static final char NOT_SIGN = '\u00AC';

    /** FULLWIDTH NOT SIGN */
    public static final char FULLWIDTH_NOT_SIGN = '\uFFE2';

    /**
     * 文字列に変換します。
     *
     * @param value
     *            変換元のオブジェクト
     * @return 変換された{@literal String}
     */
    public static String toString(final Object value) {
        return toString(value, null);
    }

    /**
     * 文字列に変換します。
     *
     * @param value
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@literal String}
     */
    public static String toString(final Object value, final String pattern) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) value;
        } else if (value instanceof java.util.Date) {
            return toString((java.util.Date) value, pattern);
        } else if (value instanceof Number) {
            return toString((Number) value, pattern);
        } else if (value instanceof byte[]) {
            return Base64.getUrlEncoder().encodeToString((byte[]) value);
        } else {
            return value.toString();
        }
    }

    /**
     * 文字列に変換します。
     *
     * @param value
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@literal String}
     */
    public static String toString(final Number value, final String pattern) {
        if (value != null) {
            if (pattern != null) {
                return new DecimalFormat(pattern).format(value);
            }
            return value.toString();
        }
        return null;
    }

    /**
     * 文字列に変換します。
     *
     * @param value
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@literal String}
     */
    public static String toString(final java.util.Date value,
            final String pattern) {
        if (value != null) {
            if (pattern != null) {
                return new SimpleDateFormat(pattern).format(value);
            }
            return value.toString();
        }
        return null;
    }

    /**
     * Windows固有のマッピングルールで作成された文字列を修正します。
     *
     * @param source
     *            Windows固有のマッピングルールで作成された文字列
     * @return 修正された文字列
     */
    public static String fromWindowsMapping(final String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        final char[] array = source.toCharArray();
        for (int i = 0; i < array.length; ++i) {
            switch (array[i]) {
            case WAVE_DASH:
                array[i] = FULLWIDTH_TILDE;
                break;
            case DOUBLE_VERTICAL_LINE:
                array[i] = PARALLEL_TO;
                break;
            case MINUS_SIGN:
                array[i] = FULLWIDTH_HYPHEN_MINUS;
                break;
            case CENT_SIGN:
                array[i] = FULLWIDTH_CENT_SIGN;
                break;
            case POUND_SIGN:
                array[i] = FULLWIDTH_POUND_SIGN;
                break;
            case NOT_SIGN:
                array[i] = FULLWIDTH_NOT_SIGN;
                break;
            default:
                break;
            }
        }
        return new String(array);
    }

    /**
     * 文字列をWindows固有のマッピングルールに合わせて修正します。
     *
     * @param source
     *            文字列
     * @return Windows固有のマッピングルールに修正された文字列
     */
    public static String toWindowsMapping(final String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        final char[] array = source.toCharArray();
        for (int i = 0; i < array.length; ++i) {
            switch (array[i]) {
            case FULLWIDTH_TILDE:
                array[i] = WAVE_DASH;
                break;
            case PARALLEL_TO:
                array[i] = DOUBLE_VERTICAL_LINE;
                break;
            case FULLWIDTH_HYPHEN_MINUS:
                array[i] = MINUS_SIGN;
                break;
            case FULLWIDTH_CENT_SIGN:
                array[i] = CENT_SIGN;
                break;
            case FULLWIDTH_POUND_SIGN:
                array[i] = POUND_SIGN;
                break;
            case FULLWIDTH_NOT_SIGN:
                array[i] = NOT_SIGN;
                break;
            default:
                break;
            }
        }
        return new String(array);
    }

}
