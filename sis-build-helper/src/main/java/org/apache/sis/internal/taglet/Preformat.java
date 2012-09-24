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
package org.apache.sis.internal.taglet;

import java.util.*;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;


/**
 * The <code>@preformat</code> tag for inserting a pre-formatted code in a javadoc comment.
 * The first word after the tag must be the format name ("java", "math", "wkt" or "text").
 * The remaining is the text to format.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.00)
 * @version 0.3
 */
public final class Preformat implements Taglet {
    /**
     * Special characters to replace by HTML entities.
     */
    private static final String[] SPECIAL_CHARS = new String[] {
        "&", "&amp;",
        "<", "&lt;",
        ">", "&gt;",
        "↑", "&uarr;"
    };

    /**
     * Register this taglet.
     *
     * @param tagletMap the map to register this tag to.
     */
    public static void register(final Map<String,Taglet> tagletMap) {
       final Preformat tag = new Preformat();
       tagletMap.put(tag.getName(), tag);
    }

    /**
     * Constructs a default <code>@preformat</code> taglet.
     */
    private Preformat() {
        super();
    }

    /**
     * Returns the name of this custom tag.
     *
     * @return The tag name.
     */
    @Override
    public String getName() {
        return "preformat";
    }

    /**
     * Returns {@code false} since <code>@preformat</code> can not be used in overview.
     *
     * @return Always {@code false}.
     */
    @Override
    public boolean inOverview() {
        return false;
    }

    /**
     * Returns {@code true} since <code>@preformat</code> can be used in package documentation.
     *
     * @return Always {@code true}.
     */
    @Override
    public boolean inPackage() {
        return true;
    }

    /**
     * Returns {@code true} since <code>@preformat</code> can be used in type documentation
     * (classes or interfaces).
     *
     * @return Always {@code true}.
     */
    @Override
    public boolean inType() {
        return true;
    }

    /**
     * Returns {@code true} since <code>@preformat</code> can be used in constructor
     *
     * @return Always {@code true}.
     */
    @Override
    public boolean inConstructor() {
        return true;
    }

    /**
     * Returns {@code true} since <code>@preformat</code> can be used in method documentation.
     *
     * @return Always {@code true}.
     */
    @Override
    public boolean inMethod() {
        return true;
    }

    /**
     * Returns {@code true} since <code>@preformat</code> can be used in field documentation.
     *
     * @return Always {@code true}.
     */
    @Override
    public boolean inField() {
        return true;
    }

    /**
     * Returns {@code true} since <code>@preformat</code> is an inline tag.
     *
     * @return Always {@code true}.
     */
    @Override
    public boolean isInlineTag() {
        return true;
    }

    /**
     * Given an array of {@code Tag}s representing this custom tag, return its string
     * representation. This method should not be called since arrays of inline tags do
     * not exist. However we define it as a matter of principle.
     *
     * @param tags The tags to format.
     * @return A string representation of the given tags.
     */
    @Override
    public String toString(final Tag[] tags) {
        final StringBuilder buffer = new StringBuilder();
        for (int i=0; i<tags.length; i++) {
            buffer.append(toString(tags[i]));
        }
        return buffer.toString();
    }

    /**
     * Given the <code>Tag</code> representation of this custom tag, return its string representation.
     *
     * @param tag The tag to format.
     * @return A string representation of the given tag.
     */
    @Override
    public String toString(final Tag tag) {
        String text = tag.text().trim().replace("\r\n", "\n").replace('\r', '\n');
        String format = "<unspecified>";
        /*
         * Extracts the first word, which is expected to be the format name.
         */
        for (int i=0; i<text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                format = text.substring(0, i);
                text = trim(text.substring(i));
                break;
            }
        }
        final boolean java  = format.equals("java");
        final boolean math  = format.equals("math");
        final boolean wkt   = format.equals("wkt");
        final boolean xml   = format.equals("xml");
        final boolean sql   = format.equals("sql");
        final boolean shell = format.equals("shell");
        if (!java && !math && !wkt && !xml && !sql && !shell && !format.equals("text")) {
            ConfigurationImpl.getInstance().root.printWarning(tag.position(), "Unknown format: " + format);
        }
        /*
         * Counts the minimal amount of spaces in the margin.
         */
        int margin = 0;
        StringTokenizer tk = new StringTokenizer(text, "\r\n");
all:    while (tk.hasMoreTokens()) {
            final String line = tk.nextToken();
            int stop = line.length();
            if (margin != 0 && margin < stop) {
                stop = margin;
            }
            for (int i=0; i<stop; i++) {
                if (!Character.isSpaceChar(line.charAt(i))) {
                    if (margin == 0 || i < margin) {
                        margin = i;
                    }
                    if (i == 0) {
                        break all;
                    }
                    break;
                }
            }
        }
        /*
         * Nows inserts each line.
         */
        final StringBuilder buffer = new StringBuilder("<blockquote><pre>");
        tk = new StringTokenizer(text, "\r\n", true);
        while (tk.hasMoreTokens()) {
            String line = tk.nextToken();
            if (!line.startsWith("\n")) {
                if (margin < line.length()) {
                    line = line.substring(margin);
                }
                for (int i=0; i<SPECIAL_CHARS.length;) {
                    line = line.replace(SPECIAL_CHARS[i++], SPECIAL_CHARS[i++]);
                }
                if (java) {
                    colorJava(line, buffer);
                    continue;
                }
            }
            buffer.append(line);
        }
        return buffer.append("</pre></blockquote>").toString();
    }

    /**
     * Lists of Java keywords.
     */
    private static final Set<String> KEYWORDS =
            Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        "abstract", "continue", "for",        "new",        "switch",
        "assert",   "default",  "goto",       "package",    "synchronized",
        "boolean",  "do",       "if",         "private",    "this",
        "break",    "double",   "implements", "protected",  "throw",
        "byte",     "else",     "import",     "public",     "throws",
        "case",     "enum",     "instanceof", "return",     "transient",
        "catch",    "extends",  "int",        "short",      "try",
        "char",     "final",    "interface",  "static",     "void",
        "class",    "finally",  "long",       "strictfp",   "volatile",
        "const",    "float",    "native",     "super",      "while",
        /* literals: */ "true", "false", "null"
    )));

    /**
     * Adds syntactic coloration for the given line.
     */
    private static void colorJava(final String line, final StringBuilder buffer) {
        char quote = 0; // The kind of quoting in progress (" or ').
        final int length = line.length();
        for (int i=0; i<length; i++) {
            final char c = line.charAt(i);
            if (quote == 0) {
                if (Character.isJavaIdentifierStart(c)) {
                    int j = i;
                    while (++j < length && Character.isJavaIdentifierPart(line.charAt(j)));
                    final String word = line.substring(i, j);
                    final boolean keyword = KEYWORDS.contains(word);
                    i = j-1;
                    boolean function = false;
                    if (!keyword || word.equals("this") || word.equals("super")) {
                        while (j < length) {
                            final char t = line.charAt(j++);
                            if (!Character.isWhitespace(t)) {
                                function = (t == '(');
                                break;
                            }
                        }
                    }
                    if (function) buffer.append("<b>");
                    if (keyword)  buffer.append("<font color=\"green\">");
                    if (true)     buffer.append(word);
                    if (keyword)  buffer.append("</font>");
                    if (function) buffer.append("</b>");
                    continue;
                }
                switch (c) {
                    case '/': {
                        if (i+1 < length && line.charAt(i+1) == '/') {
                            buffer.append("<i><font color=\"gray\">").append(line.substring(i)).append("</font></i>");
                            return;
                        }
                        break;
                    }
                    case '\'': // fall through
                    case '"': {
                        quote = c;
                        buffer.append("<font color=\"orangered\">").append(c);
                        continue;
                    }
                }
            } else if (c == quote) {
                quote = 0;
                buffer.append(c).append("</font>");
                continue;
            }
            buffer.append(c);
        }
    }

    /**
     * Removes the leading and trailing linefeeds (but not other kind of spaces).
     */
    private static String trim(final String line) {
        int high = line.length();
        while (high != 0) {
            final char c = line.charAt(high - 1);
            if (c != '\r' && c != '\n') break;
            high--;
        }
        int low = 0;
        while (low != high) {
            final char c = line.charAt(low);
            if (c != '\r' && c != '\n') break;
            low++;
        }
        return line.substring(low, high);
    }
}