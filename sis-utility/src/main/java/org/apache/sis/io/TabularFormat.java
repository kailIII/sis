/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.io;

import java.util.Locale;
import java.util.TimeZone;
import java.text.ParsePosition;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import net.jcip.annotations.NotThreadSafe;
import org.apache.sis.util.StringBuilders;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.resources.Errors;


/**
 * Base class for parser and formatter of tabular data, providing control on line and column separators.
 * The line separator is specified by a string. But the column separator is specified by a pattern which
 * provide some control on the character to repeat, and on the strings to insert before and after the
 * repeated character. See the following methods for details:
 *
 * <ul>
 *   <li>{@link #setLineSeparator(String)}</li>
 *   <li>{@link #setColumnSeparatorPattern(String)}</li>
 * </ul>
 *
 * For implementors, this base class takes care of splitting a column separator pattern into its
 * components ({@link #columnSeparator}, {@link #separatorPrefix} and {@link #separatorSuffix})
 * for easier usage in {@code format(…)} method implementations. Subclasses can use those fields
 * like below:
 *
 * <table class="sis"><tr>
 *   <th>Table with no border</th>
 *   <th>Table with a border</th>
 * </tr><tr><td>
 * {@preformat java
 *     TableFormatter table = new TableFormatter(out, "");
 *     // ... do some work, then add a column separator:
 *     table.append(separatorPrefix);
 *     table.nextColumn(columnSeparator);
 *     table.append(separatorSuffix);
 * }
 * </td><td>
 * {@preformat java
 *     TableFormatter table = new TableFormatter(out, separatorSuffix);
 *     // ... do some work, then add a column separator:
 *     table.append(separatorPrefix);
 *     table.nextColumn(columnSeparator);
 * }
 * </td></tr></table>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3
 * @version 0.3
 * @module
 *
 * @param <T> The base type of objects parsed and formatted by this class.
 *
 * @see TableFormatter
 */
@NotThreadSafe
public abstract class TabularFormat<T> extends CompoundFormat<T> {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -1599411687892965650L;

    /**
     * The line separator to use for formatting the tree.
     * The default value is system-dependent.
     *
     * @see #getLineSeparator()
     * @see #setLineSeparator(String)
     */
    protected String lineSeparator;

    /**
     * The column separator to use at formatting time if there is more than one column.
     * This is the character between the "{@code [ ]}" pair of brackets in the pattern
     * given to the {@link #setColumnSeparatorPattern(String)} method.
     *
     * Subclasses will typically use this value in calls to {@link TableFormatter#nextColumn(char)}.
     */
    protected char columnSeparator;

    /**
     * The string to write before the {@link #columnSeparator}, or an empty string if none.
     * This is the sequence of characters before the "{@code [ ]}" pair of brackets in the
     * pattern given to the {@link #setColumnSeparatorPattern(String)} method.
     */
    protected String separatorPrefix;

    /**
     * The string to write after the {@link #columnSeparator}, or an empty string if none.
     * This is the sequence of characters after the "{@code [ ]}" pair of brackets in the
     * pattern given to the {@link #setColumnSeparatorPattern(String)} method.
     */
    protected String separatorSuffix;

    /**
     * {@code true} if the trailing {@code null} values shall be omitted at formatting time.
     * This flag is controlled by the presence or absence of the {@code '?'} character at the
     * beginning of the pattern given to the {@link #setColumnSeparatorPattern(String)} method.
     */
    protected boolean omitTrailingNulls;

    /**
     * {@code true} if the user defined the parsing pattern explicitely.
     */
    private boolean isParsePatternDefined;

    /**
     * The pattern used at parsing time for finding the column separators, or {@code null}
     * if not yet constructed. This field is serialized because it may be a user-specified pattern.
     */
    private Pattern parsePattern;

    /**
     * Creates a new tabular format.
     *
     * @param locale   The locale to use for numbers, dates and angles formatting.
     * @param timezone The timezone, or {@code null} for UTC.
     */
    public TabularFormat(final Locale locale, final TimeZone timezone) {
        super(locale, timezone);
        separatorPrefix = "";
        columnSeparator = ' ';
        separatorSuffix = " ";
        lineSeparator   = System.lineSeparator();
    }

    /**
     * Returns the current line separator. The default value is system-dependent.
     *
     * @return The current line separator.
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Sets the line separator. Can not be a null or empty string.
     *
     * @param separator The new line separator.
     */
    public void setLineSeparator(final String separator) {
        ArgumentChecks.ensureNonEmpty("separator", separator);
        lineSeparator = separator;
    }

    /**
     * Returns the pattern of characters used in column separators. Those characters will be used
     * only if more than one column is formatted. See {@link #setColumnSeparatorPattern(String)}
     * for a description of the pattern syntax.
     *
     * @return The pattern of the current column separator.
     */
    public String getColumnSeparatorPattern() {
        final StringBuilder buffer = new StringBuilder(8);
        buffer.append(separatorPrefix).append('\uFFFF').append(separatorSuffix);
        StringBuilders.replace(buffer, "\\", "\\\\");
        StringBuilders.replace(buffer, "?",  "\\?");
        StringBuilders.replace(buffer, "[",  "\\[");
        StringBuilders.replace(buffer, "]",  "\\]");
        StringBuilders.replace(buffer, "/",  "\\/");
        if (omitTrailingNulls) {
            buffer.insert(0, '?');
        }
        final int insertAt = buffer.indexOf("\uFFFF");
        buffer.replace(insertAt, insertAt+1, "[\uFFFF]").setCharAt(insertAt+1, columnSeparator);
        if (isParsePatternDefined) {
            buffer.append('/').append(parsePattern.pattern());
        }
        return buffer.toString();
    }

    /**
     * Sets the pattern of the characters to insert between the columns. The pattern shall contain
     * exactly one occurrence of the {@code "[ ]"} pair of bracket, with exactly one character
     * between them. This character will be repeated as many time as needed for columns alignment.
     *
     * <p>The formatting pattern can optionally be followed by a regular expression to be used at
     * parsing time. If omitted, the parsing pattern will be inferred from the formatting pattern.
     * If specified, then the {@link #parse(CharSequence, ParsePosition) parse} method will invoke
     * the {@link Matcher#find()} method for determining the column boundaries.</p>
     *
     * <p>The characters listed below have special meaning in the pattern.
     * Other characters are appended <cite>as-is</cite> between the columns.</p>
     *
     * <table class="sis">
     *   <tr><th>Character(s)</th> <th>Meaning</th></tr>
     *   <tr><td>{@code '?'}</td>  <td>Omit the column separator for trailing null values.</td></tr>
     *   <tr><td>{@code "[ ]"}</td><td>Repeat the character between bracket as needed.</td></tr>
     *   <tr><td>{@code '/'}</td>  <td>Separate the formatting pattern from the parsing pattern.</td></tr>
     *   <tr><td>{@code '\\'}</td> <td>Escape any of the characters listed in this table.</td></tr>
     * </table>
     *
     * {@section Restrictions}
     * <ul>
     *   <li>If present, {@code '?'} shall be the first character in the pattern.</li>
     *   <li>The repeated character (specified inside the pair of brackets) is mandatory.</li>
     *   <li>In the current implementation, the repeated character must be in the
     *       {@linkplain Character#isBmpCodePoint(int) Basic Multilanguage Plane}.</li>
     *   <li>If {@code '/'} is present, anything on its right must be compliant
     *       with the {@link Pattern} syntax.</li>
     * </ul>
     *
     * {@section Example}
     * The {@code "?……[…] "} pattern means "<cite>If the next value is non-null, then insert the
     * {@code "……"} string, repeat the {@code '…'} character as many time as needed (may be zero),
     * then insert a space</cite>".
     *
     * @param  pattern The pattern of the new column separator.
     * @throws IllegalArgumentException If the given pattern is illegal.
     */
    public void setColumnSeparatorPattern(final String pattern) throws IllegalArgumentException {
        ArgumentChecks.ensureNonEmpty("pattern", pattern);
        final int length = pattern.length();
        final StringBuilder buffer = new StringBuilder(length);
        boolean escape  = false;
        boolean trim    = false;
        String  prefix  = null;
        String  regex   = null;
        int separatorIndex = -1;
scan:   for (int i=0; i<length; i++) {
            final char c = pattern.charAt(i);
            switch (c) {
                case '\uFFFF': { // This "character" is reserved.
                    prefix = null;
                    break scan; // This will cause IllegalArgumentException to be thrown.
                }
                case '\\': {
                    if (i != separatorIndex) {
                        if (escape) break;
                        escape = true;
                    }
                    continue;
                }
                case '?': {
                    if (i != 0) {
                        prefix = null;
                        break scan;
                    }
                    trim = true;
                    continue;
                }
                case '[': {
                    if (escape) break;
                    if (i != separatorIndex) {
                        if (separatorIndex >= 0) {
                            prefix = null;
                            break scan; // This will cause IllegalArgumentException to be thrown.
                        }
                        separatorIndex = i+1;
                    }
                    continue;
                }
                case ']': {
                    if (escape) break;
                    switch (i - separatorIndex) {
                        case 0:  continue;
                        case 1:  prefix = buffer.toString(); buffer.setLength(0); continue;
                        default: prefix = null; break scan;
                    }
                }
                case '/': {
                    if (escape) break;
                    regex = pattern.substring(i+1);
                    break scan;
                }
            }
            if (i != separatorIndex) {
                buffer.append(c);
            }
        }
        if (prefix == null) {
            throw new IllegalArgumentException(Errors.format(
                    Errors.Keys.IllegalFormatPatternForClass_2, getValueType(), pattern));
        }
        /*
         * Finally store the result. The parsing pattern must be first because the call to
         * Pattern.compile(regex) may thrown PatternSyntaxException. In such case, we want
         * it to happen before we modified anything else.
         */
        if (regex != null) {
            parsePattern = Pattern.compile(regex);
            isParsePatternDefined = true;
        } else {
            parsePattern = null;
            isParsePatternDefined = false;
        }
        omitTrailingNulls = trim;
        separatorPrefix   = prefix;
        separatorSuffix   = buffer.toString();
        columnSeparator   = pattern.charAt(separatorIndex);
    }

    /**
     * Returns a matcher for the column separators in the given text.
     * This method is invoked by subclasses in their {@code parse(…)} implementations.
     *
     * @param  text The text for which to get a matcher.
     * @return A matcher for the column separators in the given text.
     */
    protected Matcher getColumnSeparatorMatcher(final CharSequence text) {
        if (parsePattern == null) {
            final StringBuilder pattern = new StringBuilder(separatorPrefix).append(columnSeparator);
            String tmp = pattern.toString();
            pattern.setLength(0);
            pattern.append(Pattern.quote(tmp)).append('*');
            tmp = separatorSuffix;
            if (tmp.length() != 0) {
                pattern.append(Pattern.quote(tmp));
            }
            parsePattern = Pattern.compile(pattern.toString());
        }
        return parsePattern.matcher(text);
    }
}
