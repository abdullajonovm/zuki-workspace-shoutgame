package tv.shout.util;

public class MathUtil
{
    //https://stackoverflow.com/questions/27583122/how-to-find-the-nearest-number-that-is-power-of-two-to-another-number?lq=1
    //tested against several other methods, and this was by far the fastest
    public static int getNearestEqualToOrLargerPowerOf2(int n)
    {
        n = n - 1;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        return n + 1;
    }

}
