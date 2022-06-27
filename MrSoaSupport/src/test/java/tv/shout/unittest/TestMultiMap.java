package tv.shout.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import tv.shout.util.MultiMap;

public class TestMultiMap
{
    @Test
    public void testMultiMap()
    {
        MultiMap<String, Integer> multiMap = new MultiMap<>();
        multiMap.put("odd", 1);
        multiMap.put("odd", 3);
        multiMap.put("even", 2);
        multiMap.put("even", 4);

        assertEquals(2, multiMap.size());

        List<Integer> odds = multiMap.get("odd");
        assertNotNull(odds);
        assertEquals(2, odds.size());
        assertEquals(1, odds.get(0));
        assertEquals(3, odds.get(1));

        List<Integer> events = multiMap.get("even");
        assertNotNull(events);
        assertEquals(2, events.size());
        assertEquals(2, events.get(0));
        assertEquals(4, events.get(1));
    }
}
