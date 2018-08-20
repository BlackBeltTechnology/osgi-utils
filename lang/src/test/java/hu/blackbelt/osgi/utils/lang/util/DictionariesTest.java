package hu.blackbelt.osgi.utils.lang.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Dictionary;

import static hu.blackbelt.osgi.utils.lang.util.Dictionaries.dictionary;
import static org.junit.Assert.assertEquals;

public class DictionariesTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testDictionariesTwoParams() {
        Dictionary<String, Integer> dictionary = dictionary("one", 1);
        assertEquals(1, dictionary.size());
        assertEquals(Integer.valueOf(1), dictionary.get("one"));
    }

    @Test
    public void testDictionariesVararg() {
        Dictionary<String, Integer> dictionary = dictionary(
                "one", 1, "two", 2, "three", 3, "four", 4, "five", 5, "six", 6);
        assertEquals(6, dictionary.size());
        assertEquals(Integer.valueOf(1), dictionary.get("one"));
        assertEquals(Integer.valueOf(2), dictionary.get("two"));
        assertEquals(Integer.valueOf(3), dictionary.get("three"));
        assertEquals(Integer.valueOf(4), dictionary.get("four"));
        assertEquals(Integer.valueOf(5), dictionary.get("five"));
        assertEquals(Integer.valueOf(6), dictionary.get("six"));

    }
    @Test
    public void testDictionariesVarargOdd() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Equal number of key-value pairs expected.");
        dictionary("one", 1, "two", 2, "three", 3, "four", 4, "five", 5, "six", 6, "seven");
    }

}