package tv.shout.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import tv.shout.util.MathUtil;

public class TestMathUtil
{
    @Test
    public void testGetNearestEqualToOrLargerPowerOf2()
    {
        List<Integer> ints = Arrays.asList(-1, 0, 1, 2, 3, 4, 7, 8, 15, 16, 17, 31, 32, 33);
        List<Integer> results = Arrays.asList(0,0,1,2,4,4,8,8,16,16,32,32,32,64);
        for (int i=0; i<ints.size(); i++) {
            int nearestPowerOf2 = MathUtil.getNearestEqualToOrLargerPowerOf2(ints.get(i));
            assertEquals(results.get(i), nearestPowerOf2);
        }
    }
}
