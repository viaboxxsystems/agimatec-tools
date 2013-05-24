package com.agimatec.sql.script;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.io.StringReader;

/**
 * <p>Title: Agimatec GmbH</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Agimatec GmbH </p>
 *
 * @author Roman Stumm
 */
public class WordTokenizerTest extends TestCase {

    public WordTokenizerTest(String name) {
        super(name);
    }

    public static TestSuite suite() {
        return new TestSuite(WordTokenizerTest.class);
    }

    public void testSimple() throws IOException {
        String[] seps = {"<=", ">=", "<", ">", "="};

        StringReader reader;
        WordTokenizer tokens;

        reader = new StringReader("A>B");
        tokens = new WordTokenizer(reader, seps, true, true);
        assertEquals("A", tokens.nextToken());
        assertEquals(">", tokens.nextToken());
        assertEquals("B", tokens.nextToken());
    }

    public void testTokenizeCaseInsensitive() throws IOException {
        StringReader reader = new StringReader("BEGIN;start;End");
        String[] seps = new String[]{"begin", "end", ";"};
        WordTokenizer tokens = new WordTokenizer(reader, seps, true, false);
        assertEquals("BEGIN", tokens.nextToken());
        assertEquals(";", tokens.nextToken());
        assertEquals("start", tokens.nextToken());
        assertEquals(";", tokens.nextToken());
        assertEquals("End", tokens.nextToken());
        assertTrue(tokens.isSeparator("End"));
        assertTrue(tokens.isSeparator("Begin"));

        reader = new StringReader("TripBegin and End now.");
        seps = new String[]{"begin"};
        tokens = new WordTokenizer(reader, seps, true, false);
        assertEquals("Trip", tokens.nextToken());
        assertEquals("Begin", tokens.nextToken());
        assertEquals(" and End now.", tokens.nextToken());

        reader.reset();

        tokens = new WordTokenizer(reader, seps, false, false);
        assertEquals("Trip", tokens.nextToken());
        assertEquals(" and End now.", tokens.nextToken());
    }

    public void testTokenizeTrue() throws IOException {
        StringReader reader =
                new StringReader("Test;mit /* comment */tokens\nja//comment2\nENDE");
        String[] seps = new String[]{"\n", ";", "/*", "*/", "//", " "};
        WordTokenizer tokens = new WordTokenizer(reader, seps, true, true);
        String next;
        next = tokens.nextToken();
        assertEquals("Test", next);

        next = tokens.nextToken();
        assertEquals(";", next);

        next = tokens.nextToken();
        assertEquals("mit", next);

        next = tokens.nextToken();
        assertEquals(" ", next);

        next = tokens.nextToken();
        assertEquals("/*", next);

        next = tokens.nextToken();
        assertEquals(" ", next);

        next = tokens.nextToken();
        assertEquals("comment", next);

        next = tokens.nextToken();
        assertEquals(" ", next);

        next = tokens.nextToken();
        assertEquals("*/", next);

        next = tokens.nextToken();
        assertEquals("tokens", next);

        next = tokens.nextToken();
        assertEquals("\n", next);

        next = tokens.nextToken();
        assertEquals("ja", next);

        next = tokens.nextToken();
        assertEquals("//", next);

        next = tokens.nextToken();
        assertEquals("comment2", next);

        next = tokens.nextToken();
        assertEquals("\n", next);

        next = tokens.nextToken();
        assertEquals("ENDE", next);

        next = tokens.nextToken();
        assertEquals(null, next);

        next = tokens.nextToken();
        assertEquals(null, next);
    }

    public void testTokenizeFalse() throws IOException {
        StringReader reader =
                new StringReader("Test;mit /* comment */tokens\nja//comment2\nENDE");
        String[] seps = new String[]{"\n", ";", "/*", "*/", "//", " "};
        WordTokenizer tokens = new WordTokenizer(reader, seps, false, true);
        String next;
        next = tokens.nextToken();
        assertEquals("Test", next);

        next = tokens.nextToken();
        assertEquals("mit", next);

        next = tokens.nextToken();
        assertEquals("comment", next);

        next = tokens.nextToken();
        assertEquals("tokens", next);

        next = tokens.nextToken();
        assertEquals("ja", next);

        next = tokens.nextToken();
        assertEquals("comment2", next);

        next = tokens.nextToken();
        assertEquals("ENDE", next);

        next = tokens.nextToken();
        assertEquals(null, next);

        next = tokens.nextToken();
        assertEquals(null, next);
    }
}
