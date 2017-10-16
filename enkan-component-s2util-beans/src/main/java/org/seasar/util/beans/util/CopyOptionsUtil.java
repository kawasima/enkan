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
package org.seasar.util.beans.util;

import java.util.Map;

import org.seasar.util.beans.Converter;
import org.seasar.util.beans.converter.DateConverter;
import org.seasar.util.beans.converter.NumberConverter;
import org.seasar.util.beans.converter.SqlDateConverter;
import org.seasar.util.beans.converter.TimeConverter;
import org.seasar.util.beans.converter.TimestampConverter;

/**
 * {@link CopyOptions}のインスタンス化を容易にするために{@literal static import}して使うためのユーティリティです。
 *
 * <pre>
 * import static org.seasar.util.beans.util.CopyOptionsUtil.*;
 *
 * copyBeanToBean(srcBean, destBean, excludeNull());
 * </pre>
 * <p>
 * {@literal CopyOptionsUtil}の戻り値は{@link CopyOptions}
 * なので、メソッドチェーンでオプションを複数指定することもできます。
 * </p>
 *
 * <pre>
 * copyBeanToBean(srcBean, destBean, excludeNull().dateConverter("date", "MM/dd"));
 * </pre>
 *
 * @author koichik
 */
public abstract class CopyOptionsUtil {

    /**
     * 操作の対象に含めるプロパティ名を指定した{@link CopyOptions}を返します。
     *
     * @param propertyNames
     *            プロパティ名の配列。{@literal null}や空配列であってはいけません
     * @return 操作の対象に含めるプロパティ名を指定した{@link CopyOptions}
     * @see CopyOptions#include(CharSequence...)
     */
    public static CopyOptions include(final CharSequence... propertyNames) {
        return new CopyOptions().include(propertyNames);
    }

    /**
     * 操作の対象に含めないプロパティ名を指定した{@link CopyOptions}を返します。
     *
     * @param propertyNames
     *            プロパティ名の配列。{@literal null}や空配列であってはいけません
     * @return 操作の対象に含めないプロパティ名を指定した{@link CopyOptions}
     * @see CopyOptions#exclude(CharSequence...)
     */
    public static CopyOptions exclude(final CharSequence... propertyNames) {
        return new CopyOptions().exclude(propertyNames);
    }

    /**
     * {@literal null}値のプロパティを操作の対象外にした{@link CopyOptions}を返します。
     *
     * @return {@literal null}値のプロパティを操作の対象外にした{@link CopyOptions}を返します。
     * @see CopyOptions#excludeNull()
     */
    public static CopyOptions excludeNull() {
        return new CopyOptions().excludeNull();
    }

    /**
     * 空白のプロパティを操作の対象外にした{@link CopyOptions}を返します。
     *
     * @return 空白のプロパティを操作の対象外にした{@link CopyOptions}
     * @see CopyOptions#excludeWhitespace()
     */
    public static CopyOptions excludeWhitespace() {
        return new CopyOptions().excludeWhitespace();
    }

    /**
     * プレフィックスを指定した{@link CopyOptions}を返します。
     * <p>
     * プレフィックスを指定すると、コピー元のプロパティ名がプレフィックスで始まるプロパティだけがコピーの対象となります。
     * また、コピー元のプロパティ名からプレフィックスを除去した名前がコピー先のプロパティ名となります。
     * </p>
     *
     * @param prefix
     *            プレフィックス。{@literal null}や空文字列であってはいけません
     * @return プレフィックスを指定した{@link CopyOptions}
     * @see CopyOptions#prefix(CharSequence)
     */
    public static CopyOptions prefix(final CharSequence prefix) {
        return new CopyOptions().prefix(prefix);
    }

    /**
     * JavaBeansのデリミタを設定した{@link CopyOptions}を返します。
     * <p>
     * JavaBeansから{@link Map}へ、あるいはその逆にコピーする際に、プロパティ名のデリミタを変更することが出来ます。
     * 例えばJavaBeans側のデリミタにアンダースコア、{@link Map}側のデリミタにピリオドを指定した場合、
     * コピー元とコピー先のプリパティ名は次のようになります。
     * </p>
     * <table border="1" summary="The property name of JavaBeans">
     * <tr>
     * <th>JavaBeansのプロパティ名</th>
     * <th>{@literal Map}のプロパティ名</th>
     * </tr>
     * <tr>
     * <td>{@literal foo}</td>
     * <td>{@literal foo}</td>
     * </tr>
     * <tr>
     * <td>{@literal foo_bar}</td>
     * <td>{@literal foo.bar}</td>
     * </tr>
     * <tr>
     * <td>{@literal foo_bar_baz}</td>
     * <td>{@literal foo.bar.baz}</td>
     * </tr>
     * </table>
     *
     * @param beanDelimiter
     *            JavaBeansのデリミタ
     * @return JavaBeansのデリミタを設定した{@link CopyOptions}
     * @see CopyOptions#beanDelimiter(char)
     */
    public static CopyOptions beanDelimiter(final char beanDelimiter) {
        return new CopyOptions().beanDelimiter(beanDelimiter);
    }

    /**
     * {@literal Map}のデリミタを設定した{@link CopyOptions}を返します。
     * <p>
     * JavaBeansから{@link Map}へ、あるいはその逆にコピーする際に、プロパティ名のデリミタを変更することが出来ます。
     * 例えばJavaBeans側のデリミタにアンダースコア、{@link Map}側のデリミタにピリオドを指定した場合、
     * コピー元とコピー先のプリパティ名は次のようになります。
     * </p>
     * <table border="1" summary="The property name of JavaBeans">
     * <tr>
     * <th>JavaBeansのプロパティ名</th>
     * <th>{@literal Map}のプロパティ名</th>
     * </tr>
     * <tr>
     * <td>{@literal foo}</td>
     * <td>{@literal foo}</td>
     * </tr>
     * <tr>
     * <td>{@literal foo_bar}</td>
     * <td>{@literal foo.bar}</td>
     * </tr>
     * <tr>
     * <td>{@literal foo_bar_baz}</td>
     * <td>{@literal foo.bar.baz}</td>
     * </tr>
     * </table>
     *
     * @param mapDelimiter
     *            {@literal Map}のデリミタ
     * @return {@literal Map}のデリミタを設定した{@link CopyOptions}
     * @see CopyOptions#mapDelimiter(char)
     */
    public static CopyOptions mapDelimiter(final char mapDelimiter) {
        return new CopyOptions().mapDelimiter(mapDelimiter);
    }

    /**
     * コンバータを設定した{@link CopyOptions}を返します。
     *
     * @param converter
     *            コンバータ。{@literal null}であってはいけません
     * @param propertyNames
     *            このコンバータを適用するプロパティ名の並び。各要素は{@literal null}や空文字列であってはいけません
     * @return コンバータを設定した{@link CopyOptions}
     * @see CopyOptions#converter(Converter, CharSequence...)
     */
    public static CopyOptions converter(final Converter converter,
            final CharSequence... propertyNames) {
        return new CopyOptions().converter(converter, propertyNames);
    }

    /**
     * 日付のコンバータを設定した{@link CopyOptions}を返します。
     *
     * @param pattern
     *            日付のパターン。{@literal null}や空文字列であってはいけません
     * @param propertyNames
     *            プロパティ名の配列。各要素は{@literal null}や空文字列であってはいけません
     * @return 日付のコンバータを設定した{@link CopyOptions}
     * @see CopyOptions#dateConverter(String, CharSequence...)
     * @see DateConverter
     */
    public static CopyOptions dateConverter(final String pattern,
            final CharSequence... propertyNames) {
        return new CopyOptions().dateConverter(pattern, propertyNames);
    }

    /**
     * SQL用日付のコンバータを設定した{@link CopyOptions}を返します。
     *
     * @param pattern
     *            日付のパターン。{@literal null}や空文字列であってはいけません
     * @param propertyNames
     *            プロパティ名の配列。各要素は{@literal null}や空文字列であってはいけません
     * @return SQL用日付のコンバータを設定した{@link CopyOptions}
     * @see CopyOptions#sqlDateConverter(String, CharSequence...)
     * @see SqlDateConverter
     */
    public static CopyOptions sqlDateConverter(final String pattern,
            final CharSequence... propertyNames) {
        return new CopyOptions().sqlDateConverter(pattern, propertyNames);
    }

    /**
     * 時間のコンバータを設定した{@link CopyOptions}を返します。
     *
     * @param pattern
     *            時間のパターン。{@literal null}や空文字列であってはいけません
     * @param propertyNames
     *            プロパティ名の配列。各要素は{@literal null}や空文字列であってはいけません
     * @return 時間のコンバータを設定した{@link CopyOptions}
     * @see CopyOptions#timeConverter(String, CharSequence...)
     * @see TimeConverter
     */
    public static CopyOptions timeConverter(final String pattern,
            final CharSequence... propertyNames) {
        return new CopyOptions().timeConverter(pattern, propertyNames);
    }

    /**
     * 日時のコンバータを設定した{@link CopyOptions}を返します。
     *
     * @param pattern
     *            日時のパターン。{@literal null}や空文字列であってはいけません
     * @param propertyNames
     *            プロパティ名の配列。各要素は{@literal null}や空文字列であってはいけません
     * @return 日時のコンバータを設定した{@link CopyOptions}
     * @see CopyOptions#timestampConverter(String, CharSequence...)
     * @see TimestampConverter
     */
    public static CopyOptions timestampConverter(final String pattern,
            final CharSequence... propertyNames) {
        return new CopyOptions().timestampConverter(pattern, propertyNames);
    }

    /**
     * 数値のコンバータを設定した{@link CopyOptions}を返します。
     *
     * @param pattern
     *            数値のパターン。{@literal null}や空文字列であってはいけません
     * @param propertyNames
     *            プロパティ名の配列。各要素は{@literal null}や空文字列であってはいけません
     * @return 数値のコンバータを設定した{@link CopyOptions}
     * @see CopyOptions#numberConverter(String, CharSequence...)
     * @see NumberConverter
     */
    public static CopyOptions numberConverter(final String pattern,
            final CharSequence... propertyNames) {
        return new CopyOptions().numberConverter(pattern, propertyNames);
    }

}
