/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2011 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.sf.hale.util;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

/**
 * A simple Wrapper around a {@link StreamTokenizer}.  Sets up
 * the state of the tokenizer to a useful one, and provides convienience
 * methods for getting variables of various types
 *
 * @author Jared
 */
public class StringParser extends StreamTokenizer {
    /**
     * Creates a new parser for the specified string.
     *
     * @param input
     */
    public StringParser(String input) {
        super(new StringReader(input));

        resetSyntax();
        wordChars('\u0000', '\uFFFF');
        whitespaceChars(' ', ' ');
        whitespaceChars('\t', '\t');
        quoteChar('\"');
        quoteChar('\'');
        commentChar('#');
    }

    /**
     * Returns true if this parser has another token available, false otherwise
     *
     * @return whether this parser has another token available
     * @throws IOException
     */
    public boolean hasNext() throws IOException {

        if (nextToken() == StreamTokenizer.TT_EOF) return false;
        pushBack();
        if (nextToken() == StreamTokenizer.TT_EOL) return false;
        pushBack();

        return true;
    }

    /**
     * Returns the next token as a string.  If this parser has another
     * token ({@link #hasNext()} returns true), then this method will
     * succeed.
     *
     * @return the next token
     * @throws IOException
     */
    public String next() throws IOException {
        nextToken();

        return sval;
    }

    /**
     * Returns the next token as a long int.  This method can only
     * succeed when the parser has another token, and that token can
     * be parsed by {@link Long#parseLong(String)}
     *
     * @return the next long int
     * @throws IOException
     */
    public long nextLong() throws IOException {
        nextToken();

        return Long.parseLong(sval);
    }

    /**
     * Returns the next token as an in.  This method can only succeed when
     * the parser has another token, and that token can be parsed by
     * {@link Integer#parseInt(String)}
     *
     * @return the next int
     * @throws IOException
     */
    public int nextInt() throws IOException {
        nextToken();

        return Integer.parseInt(sval);
    }

    /**
     * Returns the next token as a boolean.  This method can only succeed when
     * the parser has another token, and that token can be parsed by
     * {@link Boolean#parseBoolean(String)}
     *
     * @return the next boolean
     * @throws IOException
     */
    public boolean nextBoolean() throws IOException {
        nextToken();

        return Boolean.parseBoolean(sval);
    }
}
