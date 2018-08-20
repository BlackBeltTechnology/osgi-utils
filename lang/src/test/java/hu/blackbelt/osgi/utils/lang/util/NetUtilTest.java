package hu.blackbelt.osgi.utils.lang.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertEquals;

public class NetUtilTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCreateUrl() {
        String url = "http://example.com";
        URL result = NetUtil.createUrl(url);
        assertEquals(url, result.toString());
    }

    @Test
    public void testCreateUrlMalformed() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectCause(any(MalformedURLException.class));
        NetUtil.createUrl("malformedUrl");
    }
}
