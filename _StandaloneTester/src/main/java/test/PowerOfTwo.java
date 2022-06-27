package test;

import java.text.MessageFormat;

public class PowerOfTwo
{
    //2000000: 4ms
    //9000000: 3ms
    public static int getNearestLargerPowerOf2(int n)
    {
        n = n - 1;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        return n + 1;
    }

    //2000000: 22ms
    //9000000: 107ms
    public static int x_getNearestLargerPowerOf2(int n)
    {
        int count = 0;

        // First n in the below condition is for the case where n is 0
        if (n > 0 && (n & (n - 1)) == 0)
            return n;

        while(n != 0)
        {
            n >>= 1;
            count += 1;
        }

        return 1 << count;
    }

    public static void main(String[] args)
    {
        System.out.println(MessageFormat.format("{0}: {1}", 64, getNearestLargerPowerOf2(64)));
//        long b = System.currentTimeMillis();
//        for (int i=0; i<9000000; i++) {
//            getNearestLargerPowerOf2(i);
//        }
//        long e = System.currentTimeMillis();
//
//        System.out.println("took: " + (e-b));
    }

}
