package tv.shout.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import tv.shout.util.MaxSizeHashMap;

public class TestMaxSizeHashMap
{
    @Test
    public void testMaxSizeHashMap()
    {
        MaxSizeHashMap<Integer, Integer> map = new MaxSizeHashMap<Integer, Integer>().withMaxSize(3);
        assertEquals(3, map.getMaxSize());

        map.put(1, 1);
        assertEquals(1, map.size());

        map.put(2, 2);
        assertEquals(2, map.size());

        map.put(3, 3);
        assertEquals(3, map.size());

        map.put(4, 4);
        assertEquals(3, map.size());
    }
}
