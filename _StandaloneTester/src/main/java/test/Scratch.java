package test;

import java.util.Random;

public class Scratch
{
    public void doit(int s1LivesLeft, int s2LivesLeft)
    {
        if (s1LivesLeft > 0 && s2LivesLeft > 0) {
            System.out.println("both have lives left");

        } else if (s1LivesLeft > 0 && s2LivesLeft <= 0) {
            System.out.println("s1 wins");

        } else if (s1LivesLeft <= 0 && s2LivesLeft > 0) {
            System.out.println("s2 wins");

        } else {
            System.out.println("both lose. picking one randomly...");
            int idx = new Random().nextInt(2);
            System.out.println("random winner is: s" + (idx +1));

        }

    }

    public static void main(String[] args)
    throws Exception
    {
        Scratch s = new Scratch();
        s.doit(1, 1);
        s.doit(0, 1);
        s.doit(1, 0);

        for (int i=0; i<10; i++) {
            s.doit(0, 0);
        }
    }

}
