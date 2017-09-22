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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static java.text.DateFormat.*;

/**
 * 日付と時刻を表現するオブジェクトから{@link Date}、{@link Calendar}、{@link Timestamp}
 * への変換ユーティリティです。
 * <p>
 * 日付だけを表現するオブジェクトを変換する場合は{@link DateConversionUtil}を、 時刻だけを表現するオブジェクトを変換する場合は
 * {@link TimeConversionUtil}を参照してください。
 * </p>
 * <p>
 * 変換元のオブジェクトが{@link Date}、{@link Calendar}、{@link Timestamp}の場合は、
 * それらの持つミリ秒単位の値を使って変換後のオブジェクトを作成します。
 * その他の型の場合は変換元オブジェクトの文字列表現から変換後のオブジェクトを作成します。
 * </p>
 * <p>
 * パターンを指定されなかった場合、変換に使用するパターンはロケールに依存して次のようになります。
 * </p>
 * <table border="1">
 * <tr>
 * <th>カテゴリ</th>
 * <th>パターン</th>
 * <th>{@link Locale#JAPANESE}の例</th>
 * </tr>
 * <tr>
 * <td rowspan="4">{@link DateFormat}の標準形式</td>
 * <td>{@link DateFormat#SHORT}の形式</td>
 * <td>{@literal yy/MM/dd H:mm}</td>
 * </tr>
 * <tr>
 * <td>{@link DateFormat#MEDIUM}の形式</td>
 * <td>{@literal yyyy/MM/dd H:mm:ss}</td>
 * </tr>
 * <tr>
 * <td>{@link DateFormat#LONG}の形式</td>
 * <td>{@literal yyyy/MM/dd H:mm:ss z}</td>
 * </tr>
 * <tr>
 * <td>{@link DateFormat#FULL}の形式</td>
 * <td>{@literal yyyy'年'M'月'd'日' H'時'mm'分'ss'秒' z}</td>
 * </tr>
 * <tr>
 * <td rowspan="4">プレーン形式</td>
 * <td>{@link DateFormat#SHORT}の区切り文字を除去した形式</td>
 * <td>{@literal yyMMdd HHmm}</td>
 * </tr>
 * <tr>
 * <td>{@link DateFormat#MEDIUM}の区切り文字を除去した形式</td>
 * <td>{@literal yyyyMMdd HHmmss}</td>
 * </tr>
 * <tr>
 * <td>{@link DateFormat#LONG}の区切り文字を除去した形式</td>
 * <td>{@literal yyyyMMdd HHmmss z}</td>
 * </tr>
 * <tr>
 * <td>{@link DateFormat#FULL}の区切り文字を除去した形式</td>
 * <td>{@literal yyyyMMdd HHmmss z}</td>
 * </tr>
 * <tr>
 * <td>その他</td>
 * <td>{@link Timestamp#valueOf(String) Jdbcエスケープ構文}形式</td>
 * <td>{@literal yyyy-MM-dd HH:mm:ss[.SSS...]}</td>
 * </tr>
 * </table>
 *
 * @author higa
 * @see DateConversionUtil
 * @see TimeConversionUtil
 */
public abstract class TimestampConversionUtil {

    /** {@link DateFormat}が持つスタイルの配列 */
    protected static final int[] STYLES =
        new int[] { SHORT, MEDIUM, LONG, FULL };

    /**
     * デフォルロケールで{@link DateFormat#SHORT}スタイルのパターン文字列を返します。
     *
     * @return {@link DateFormat#SHORT}スタイルのパターン文字列
     */
    public static String getShortPattern() {
        return getShortPattern(Locale.getDefault());
    }

    /**
     * 指定されたロケールで{@link DateFormat#SHORT}スタイルのパターン文字列を返します。
     *
     * @param locale
     *            ロケール。{@literal null}であってはいけません
     * @return {@link DateFormat#SHORT}スタイルのパターン文字列
     */
    public static String getShortPattern(final Locale locale) {
        if (locale == null) throw new IllegalArgumentException("locale is null");
        return ((SimpleDateFormat) getDateTimeInstance(SHORT, SHORT, locale))
            .toPattern();
    }

    /**
     * デフォルロケールで{@link DateFormat#MEDIUM}スタイルのパターン文字列を返します。
     *
     * @return {@link DateFormat#MEDIUM}スタイルのパターン文字列
     */
    public static String getMediumPattern() {
        return getMediumPattern(Locale.getDefault());
    }

    /**
     * 指定されたロケールで{@link DateFormat#MEDIUM}スタイルのパターン文字列を返します。
     *
     * @param locale
     *            ロケール。{@literal null}であってはいけません
     * @return {@link DateFormat#MEDIUM}スタイルのパターン文字列
     */
    public static String getMediumPattern(final Locale locale) {
        if (locale == null) throw new IllegalArgumentException("locale is null");
        return ((SimpleDateFormat) getDateTimeInstance(MEDIUM, MEDIUM, locale))
            .toPattern();
    }

    /**
     * デフォルロケールで{@link DateFormat#LONG}スタイルのパターン文字列を返します。
     *
     * @return {@link DateFormat#LONG}スタイルのパターン文字列
     */
    public static String getLongPattern() {
        return getLongPattern(Locale.getDefault());
    }

    /**
     * 指定されたロケールで{@link DateFormat#LONG}スタイルのパターン文字列を返します。
     *
     * @param locale
     *            ロケール。{@literal null}であってはいけません
     * @return {@link DateFormat#LONG}スタイルのパターン文字列
     */
    public static String getLongPattern(final Locale locale) {
        return ((SimpleDateFormat) getDateTimeInstance(LONG, LONG, locale))
            .toPattern();
    }

    /**
     * デフォルロケールで{@link DateFormat#FULL}スタイルのパターン文字列を返します。
     *
     * @return {@link DateFormat#FULL}スタイルのパターン文字列
     */
    public static String getFullPattern() {
        return getFullPattern(Locale.getDefault());
    }

    /**
     * 指定されたロケールで{@link DateFormat#FULL}スタイルのパターン文字列を返します。
     *
     * @param locale
     *            ロケール。{@literal null}であってはいけません
     * @return {@link DateFormat#FULL}スタイルのパターン文字列
     */
    public static String getFullPattern(final Locale locale) {
        if (locale == null) throw new IllegalArgumentException("locale is null");
        return ((SimpleDateFormat) getDateTimeInstance(FULL, FULL, locale))
            .toPattern();
    }

    /**
     * オブジェクトを{@link Date}に変換します。
     *
     * @param src
     *            変換元のオブジェクト
     * @return 変換された{@link Date}
     */
    public static Date toDate(final Object src) {
        return toDate(src, null, Locale.getDefault());
    }

    /**
     * オブジェクトを{@link Date}に変換します。
     *
     * @param src
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@link Date}
     */
    public static Date toDate(final Object src, final String pattern) {
        return toDate(src, pattern, Locale.getDefault());
    }

    /**
     * オブジェクトを{@link Date}に変換します。
     *
     * @param src
     *            変換元のオブジェクト
     * @param locale
     *            ロケール。{@literal null}であってはいけません
     * @return 変換された{@link Date}
     */
    public static Date toDate(final Object src, final Locale locale) {
        if (locale == null) throw new IllegalArgumentException("locale is null");
        return toDate(src, null, locale);
    }

    /**
     * オブジェクトを{@link Date}に変換します。
     *
     * @param src
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @param locale
     *            ロケール
     * @return 変換された{@link Date}
     */
    protected static Date toDate(final Object src, final String pattern,
            final Locale locale) {
        if (src == null) {
            return null;
        }
        if (src.getClass() == Date.class) {
            return (Date) src;
        }
        if (src instanceof Date) {
            return new Date(((Date) src).getTime());
        }
        if (src instanceof Calendar) {
            return new Date(((Calendar) src).getTimeInMillis());
        }
        final String str = src.toString();
        if (str.isEmpty()) {
            return null;
        }
        if (pattern != null) {
            final SimpleDateFormat format =
                new SimpleDateFormat(pattern, locale);
            final Date date = toDate(str, format);
            if (date != null) {
                return date;
            }
        }
        final Date date = toDate(str, locale);
        if (date != null) {
            return date;
        }
        final Timestamp timestamp = toSqlTimestampJdbcEscape(str);
        if (timestamp != null) {
            return new Date(timestamp.getTime());
        }
        throw new IllegalArgumentException("Can't parse as Date: " + str);
    }

    /**
     * オブジェクトを{@link Calendar}に変換します。
     *
     * @param src
     *            変換元のオブジェクト
     * @return 変換された{@link Date}
     */
    public static Calendar toCalendar(final Object src) {
        return toCalendar(src, null, Locale.getDefault());
    }

    /**
     * オブジェクトを{@link Calendar}に変換します。
     *
     * @param src
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@link Date}
     */
    public static Calendar toCalendar(final Object src, final String pattern) {
        return toCalendar(src, pattern, Locale.getDefault());
    }

    /**
     * オブジェクトを{@link Calendar}に変換します。
     *
     * @param src
     *            変換元のオブジェクト
     * @param locale
     *            ロケール。{@literal null}であってはいけません
     * @return 変換された{@link Date}
     */
    public static Calendar toCalendar(final Object src, final Locale locale) {
        if (locale == null) throw new IllegalArgumentException("locale is null");
        return toCalendar(src, null, locale);
    }

    /**
     * オブジェクトを{@link Calendar}に変換します。
     *
     * @param src
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @param locale
     *            ロケール
     * @return 変換された{@link Date}
     */
    protected static Calendar toCalendar(final Object src,
            final String pattern, final Locale locale) {
        if (src == null) {
            return null;
        }
        if (src instanceof Calendar) {
            return (Calendar) src;
        }
        if (src instanceof Date) {
            return toCalendar((Date) src, locale);
        }
        final String str = src.toString();
        if (str.isEmpty()) {
            return null;
        }
        if (pattern != null) {
            final SimpleDateFormat format = new SimpleDateFormat(pattern);
            final Date date = toDate(str, format);
            if (date != null) {
                return toCalendar(date, locale);
            }
        }
        final Date date = toDate(str, locale);
        if (date != null) {
            return toCalendar(date, locale);
        }
        final Timestamp timestamp = toSqlTimestampJdbcEscape(str);
        if (timestamp != null) {
            return toCalendar(timestamp, locale);
        }
        throw new IllegalArgumentException("Can't parse as Calendar: " + str);
    }

    /**
     * オブジェクトを{@link Timestamp}に変換します。
     *
     * @param src
     *            変換元のオブジェクト
     * @return 変換された{@link Timestamp}
     */
    public static Timestamp toSqlTimestamp(final Object src) {
        return toSqlTimestamp(src, null, Locale.getDefault());
    }

    /**
     * オブジェクトを{@link Timestamp}に変換します。
     *
     * @param src
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @return 変換された{@link Timestamp}
     */
    public static Timestamp toSqlTimestamp(final Object src,
            final String pattern) {
        return toSqlTimestamp(src, pattern, Locale.getDefault());
    }

    /**
     * オブジェクトを{@link Timestamp}に変換します。
     *
     * @param src
     *            変換元のオブジェクト
     * @param locale
     *            ロケール。{@literal null}であってはいけません
     * @return 変換された{@link Timestamp}
     */
    public static Timestamp toSqlTimestamp(final Object src, final Locale locale) {
        if (locale == null) throw new IllegalArgumentException("locale is null");
        return toSqlTimestamp(src, null, locale);
    }

    /**
     * オブジェクトを{@link Timestamp}に変換します。
     *
     * @param src
     *            変換元のオブジェクト
     * @param pattern
     *            パターン文字列
     * @param locale
     *            ロケール
     * @return 変換された{@link Timestamp}
     */
    protected static Timestamp toSqlTimestamp(final Object src,
            final String pattern, final Locale locale) {
        if (src == null) {
            return null;
        }
        if (src.getClass() == Timestamp.class) {
            return (Timestamp) src;
        }
        if (src instanceof Date) {
            return new Timestamp(((Date) src).getTime());
        }
        if (src instanceof Calendar) {
            return new Timestamp(((Calendar) src).getTimeInMillis());
        }
        final String str = src.toString();
        if (str.isEmpty()) {
            return null;
        }
        if (pattern != null) {
            final SimpleDateFormat format =
                new SimpleDateFormat(pattern, locale);
            final Date date = toDate(str, format);
            if (date != null) {
                return new Timestamp(date.getTime());
            }
        }
        final Date date = toDate(str, locale);
        if (date != null) {
            return new Timestamp(date.getTime());
        }
        final Timestamp timestamp = toSqlTimestampJdbcEscape(str);
        if (timestamp != null) {
            return timestamp;
        }
        throw new IllegalArgumentException("Can't parse as Timestamp: " + str);
    }

    /**
     * 文字列を{@link Date}に変換します。
     *
     * @param str
     *            文字列
     * @param locale
     *            ロケール
     * @return 変換された{@link Date}
     */
    @SuppressWarnings("unchecked")
    protected static Date toDate(final String str, final Locale locale) {
        Stream<DateFormat> dateFormats = Arrays.stream(STYLES)
                .mapToObj(style -> DateFormat.getDateTimeInstance(style, style, locale));

        Stream<DateFormat> simpleDateFormats = Arrays.stream(STYLES)
                    .mapToObj(style -> DateFormat.getDateTimeInstance(style, style, locale))
                    .filter(SimpleDateFormat.class::isInstance)
                    .map(SimpleDateFormat.class::cast)
                    .map(format -> toPlainPattern(format.toPattern()))
                    .filter(pattern -> pattern.length() == str.length())
                    .map(pattern -> new SimpleDateFormat(pattern));

        return Stream.concat(dateFormats, simpleDateFormats)
                .map(format -> toDate(str, format))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(null);
    }

    /**
     * 文字列を{@link Date}に変換します。
     *
     * @param str
     *            文字列
     * @param format
     *            {@link DateFormat}
     * @return 変換された{@link Date}
     */
    protected static Date toDate(final String str, final DateFormat format) {
        final ParsePosition pos = new ParsePosition(0);
        final Date date = format.parse(str, pos);
        if (date == null) {
            return null;
        }
        final int index = pos.getIndex();
        if (index == 0) {
            return null;
        }
        if (index < str.length()) {
            return null;
        }
        return date;
    }

    /**
     * {@link Date}を{@link Calendar}に変換します。
     *
     * @param date
     *            {@link Date}
     * @param locale
     *            ロケール
     * @return 変換された{@link Calendar}
     */
    protected static Calendar toCalendar(final Date date, final Locale locale) {
        final Calendar calendar;
        if (locale == null) {
            calendar = Calendar.getInstance();
        } else {
            calendar = Calendar.getInstance(locale);
        }
        calendar.setTime(date);
        return calendar;
    }

    /**
     * 文字列を{@link Timestamp}に変換します。
     *
     * @param str
     *            文字列
     * @return 変換された{@link Timestamp}
     */
    protected static Timestamp toSqlTimestampJdbcEscape(final String str) {
        try {
            return Timestamp.valueOf(str);
        } catch (final IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * パターン文字列を区切り文字を含まないプレーンなパターン文字列に変換して返します。
     *
     * @param pattern
     *            パターン文字列
     * @return 区切り文字を含まないプレーンなパターン文字列
     */
    protected static String toPlainPattern(final String pattern) {
        final StringBuilder buf = new StringBuilder(pattern.length());
        for (int i = 0; i < pattern.length(); ++i) {
            final char ch = pattern.charAt(i);
            if (Character.isLetterOrDigit(ch) || Character.isWhitespace(ch)) {
                buf.append(ch);
            }
        }
        if (buf.indexOf("yy") == -1) {
            final int pos = buf.indexOf("y");
            if (pos != -1) {
                buf.replace(pos, pos + 1, "yy");
            }
        }
        if (buf.indexOf("MM") == -1) {
            final int pos = buf.indexOf("M");
            if (pos != -1) {
                buf.replace(pos, pos + 1, "MM");
            }
        }
        if (buf.indexOf("dd") == -1) {
            final int pos = buf.indexOf("d");
            if (pos != -1) {
                buf.replace(pos, pos + 1, "dd");
            }
        }
        if (buf.indexOf("HH") == -1) {
            final int pos = buf.indexOf("H");
            if (pos != -1) {
                buf.replace(pos, pos + 1, "HH");
            }
        }
        if (buf.indexOf("mm") == -1) {
            final int pos = buf.indexOf("m");
            if (pos != -1) {
                buf.replace(pos, pos + 1, "mm");
            }
        }
        if (buf.indexOf("ss") == -1) {
            final int pos = buf.indexOf("s");
            if (pos != -1) {
                buf.replace(pos, pos + 1, "ss");
            }
        }
        return new String(buf);
    }
}
