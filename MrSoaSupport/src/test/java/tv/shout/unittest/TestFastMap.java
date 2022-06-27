package tv.shout.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import tv.shout.util.FastMap;

public class TestFastMap
{
    @Test
    public void testFastMap()
    {
        Map<String, Integer> map = new FastMap<>("One", 1, "Two", 2);
        assertEquals(2, map.size());
        assertEquals(1, map.get("One"));
        assertEquals(2, map.get("Two"));
    }
}
