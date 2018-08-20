package hu.blackbelt.osgi.utils.lang;

import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;

public class RankOrderedIterableTest {
    RankOrderedIterable<String> iterable;
    
    @Before
    public void setup() {
        iterable = new RankOrderedIterable<String>();
    }
    
    public void testConstructor() {
        iterable = new RankOrderedIterable<String>();
        assertFalse(iterable.iterator().hasNext());
    }
    
    public void testCreate() {
        iterable = RankOrderedIterable.createRankOrderedIterable();
        assertFalse(iterable.iterator().hasNext());
    }

    @Test
    public void testAdd() {
        iterable.add("one", 1);
        assertArrayEquals(new String[] { "one", }, Iterables.toArray(iterable, String.class));
        iterable.add("minusOne", -1);
        assertArrayEquals(new String[] { "one", "minusOne", }, Iterables.toArray(iterable, String.class));
        iterable.add("zero", 0);
        assertArrayEquals(new String[] { "one", "zero", "minusOne" }, Iterables.toArray(iterable, String.class));
    }

    @Test
    public void testAddWithoutRanking() {
        iterable.add("one", 1);
        assertArrayEquals(new String[] { "one", }, Iterables.toArray(iterable, String.class));
        iterable.add("minusOne", -1);
        assertArrayEquals(new String[] { "one", "minusOne", }, Iterables.toArray(iterable, String.class));
        iterable.add("zero", null);
        assertArrayEquals(new String[] { "one", "zero", "minusOne" }, Iterables.toArray(iterable, String.class));
    }
    
    @Test
    public void testAddNull() {
        iterable.add("one", 1);
        iterable.add("minusOne", -1);
        iterable.add("null", null);
        assertArrayEquals(new String[] { "one", "null", "minusOne" }, Iterables.toArray(iterable, String.class));
    }

    @Test
    public void testRemove() {
        iterable.add("one", 1);
        assertArrayEquals(new String[] { "one" }, Iterables.toArray(iterable, String.class));
        iterable.remove("one", 1);
        assertArrayEquals(new String[] { }, Iterables.toArray(iterable, String.class));
    }

    @Test
    public void testRemoveWithoutRanking() {
        iterable.add("one", 1);
        assertArrayEquals(new String[] { "one" }, Iterables.toArray(iterable, String.class));
        iterable.add("zero", null);
        assertArrayEquals(new String[] { "one", "zero" }, Iterables.toArray(iterable, String.class));
        iterable.remove("zero", null);
        assertArrayEquals(new String[] { "one" }, Iterables.toArray(iterable, String.class));
    }

    @Test
    public void testRemoveWithInvalidRanking() {
        iterable.add("one", 1);
        assertArrayEquals(new String[] { "one" }, Iterables.toArray(iterable, String.class));
        iterable.remove("one", 2);
        assertArrayEquals(new String[] { "one" }, Iterables.toArray(iterable, String.class));
    }
}
