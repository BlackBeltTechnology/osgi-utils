package hu.blackbelt.osgi.utils.lang.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExceptionUtilTest {

    @Test
    public void testRetainArgumentsNoThrowable() {
        Object[] expected = new Object[] { "1", "2" };
        
        Object[] result = ExceptionUtil.retainArguments("1", "2");
        
        assertArrayEquals(expected, result);
    }

    @Test
    public void testRetainArgumentsWithThrowable() {
        Object[] expected = new Object[] { "1", "2" };
        
        Object[] result = ExceptionUtil.retainArguments("1", "2", new Throwable("3"));
        
        assertArrayEquals(expected, result);
    }
    
    @Test
    public void testRetainArgumentsWithMultipleThrowables() {
        Throwable extra = new Throwable("3");
        Object[] expected = new Object[] { "1", "2", extra };
        
        Object[] result = ExceptionUtil.retainArguments("1", "2", extra, new Throwable("4"));
        
        assertArrayEquals(expected, result);
    }
    
    @Test
    public void testRetainArgumentsEmpty() {
        Object[] result = ExceptionUtil.retainArguments();
        assertEquals(0, result.length);
    }
    
    @Test
    public void testRetainArgumentsNull() {
        assertNull(ExceptionUtil.retainArguments((Object[]) null));
    }

    @Test
    public void testRetainThrowableNoThrowable() {
        Throwable result = ExceptionUtil.retainThrowable("1", "2");
        
        assertNull(result);
    }

    @Test
    public void testRetainThrowableWithThrowable() {
        Throwable expected = new Throwable("3");
        
        Throwable result = ExceptionUtil.retainThrowable("1", "2", expected);
        
        assertEquals(expected, result);
    }
    
    @Test
    public void testRetainThrowableWithMultipleThrowables() {
        Throwable expected = new Throwable("4");
        
        Throwable result = ExceptionUtil.retainThrowable("1", "2", new Throwable("3"), expected);
        
        assertEquals(expected, result);
    }
    
    @Test
    public void testRetainThrowableEmpty() {
        Throwable result = ExceptionUtil.retainThrowable();
        assertNull(result);
    }
    
    @Test
    public void testRetainThrowableNull() {
        assertNull(ExceptionUtil.retainThrowable((Object[]) null));
    }
    
}
