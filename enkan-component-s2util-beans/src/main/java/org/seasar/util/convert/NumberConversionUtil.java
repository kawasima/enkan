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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * {@link Number}用の変換ユーティリティです。
 *
 * @author higa
 */
public abstract class NumberConversionUtil {

    /**
     * 適切な {@link Number}に変換します。
     *
     * @param type
     *            変換先の型
     * @param o
     *            変換元のオブジェクト
     * @return {@literal type}に変換された{@link Number}
     */
    public static Object convertNumber(final Class<?> type, final Object o) {
        if (type == Integer.class) {
            return IntegerConversionUtil.toInteger(o);
        } else if (type == BigDecimal.class) {
            return BigDecimalConversionUtil.toBigDecimal(o);
        } else if (type == Double.class) {
            return DoubleConversionUtil.toDouble(o);
        } else if (type == Long.class) {
            return LongConversionUtil.toLong(o);
        } else if (type == Float.class) {
            return FloatConversionUtil.toFloat(o);
        } else if (type == Short.class) {
            return ShortConversionUtil.toShort(o);
        } else if (type == BigInteger.class) {
            return BigIntegerConversionUtil.toBigInteger(o);
        } else if (type == Byte.class) {
            return ByteConversionUtil.toByte(o);
        }
        return o;
    }

    /**
     * 指定されたプリミティブ型に対応するラッパー型に変換して返します。
     *
     * @param type
     *            プリミティブ型
     * @param o
     *            変換元のオブジェクト
     * @return 指定されたプリミティブ型に対応するラッパー型に変換されたオブジェクト
     */
    public static Object convertPrimitiveWrapper(final Class<?> type,
            final Object o) {
        if (type == int.class) {
            final Integer i = IntegerConversionUtil.toInteger(o);
            if (i != null) {
                return i;
            }
            return 0;
        } else if (type == double.class) {
            final Double d = DoubleConversionUtil.toDouble(o);
            if (d != null) {
                return d;
            }
            return 0d;
        } else if (type == long.class) {
            final Long l = LongConversionUtil.toLong(o);
            if (l != null) {
                return l;
            }
            return 0L;
        } else if (type == float.class) {
            final Float f = FloatConversionUtil.toFloat(o);
            if (f != null) {
                return f;
            }
            return 0f;
        } else if (type == short.class) {
            final Short s = ShortConversionUtil.toShort(o);
            if (s != null) {
                return s;
            }
            return (short) 0;
        } else if (type == boolean.class) {
            final Boolean b = BooleanConversionUtil.toBoolean(o);
            if (b != null) {
                return b;
            }
            return Boolean.FALSE;
        } else if (type == byte.class) {
            final Byte b = ByteConversionUtil.toByte(o);
            if (b != null) {
                return b;
            }
            return (byte) 0;
        }
        return o;
    }

    /**
     * デリミタを削除します。
     *
     * @param value
     *            文字列の値
     * @param locale
     *            ロケール
     * @return デリミタを削除した結果の文字列
     */
    public static String removeDelimeter(String value, final Locale locale) {
        final String groupingSeparator = findGroupingSeparator(locale);
        if (groupingSeparator != null) {
            value = value.replaceAll(groupingSeparator, "");
        }
        return value;
    }

    /**
     * グルーピング用のセパレータを探します。
     *
     * @param locale
     *            ロケール
     * @return グルーピング用のセパレータ
     */
    public static String findGroupingSeparator(final Locale locale) {
        final DecimalFormatSymbols symbol = getDecimalFormatSymbols(locale);
        return Character.toString(symbol.getGroupingSeparator());
    }

    /**
     * 数値のセパレータを返します。
     *
     * @param locale
     *            ロケール
     * @return 数値のセパレータ
     */
    public static String findDecimalSeparator(final Locale locale) {
        final DecimalFormatSymbols symbol = getDecimalFormatSymbols(locale);
        return Character.toString(symbol.getDecimalSeparator());
    }

    private static DecimalFormatSymbols getDecimalFormatSymbols(
            final Locale locale) {
        DecimalFormatSymbols symbol;
        if (locale != null) {
            symbol = DecimalFormatSymbols.getInstance(locale);
        } else {
            symbol = DecimalFormatSymbols.getInstance();
        }
        return symbol;
    }

}
