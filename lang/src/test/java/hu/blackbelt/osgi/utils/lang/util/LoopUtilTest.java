package hu.blackbelt.osgi.utils.lang.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static hu.blackbelt.osgi.utils.lang.util.LoopUtil.loopAndCollect;
import static org.junit.Assert.assertEquals;

public class LoopUtilTest {

    @Test
    public void testSingle() {
        List<String> actual = loopAndCollect(ImmutableList.of("1", "2", "3"), id -> single(id, "a"));
        assertEquals(ImmutableList.of("1-a", "3-a"), actual);
    }

    private String single(String id, String a) {
        if ("2".equals(id)) {
            return null;
        }
        return id + "-" + a;
    }
}