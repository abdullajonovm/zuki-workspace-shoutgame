package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class BracketSort
{
    /**
     * Do an in-place bracket sort (0, size-1, 1, size-2, etc..) this assumes the list is presorted high to low
     * @param <T>
     * @param matchQueues
     */
    public <T> List<T> bracketSort(List<T> matchQueues)
    {
        List<T> list = new ArrayList<T>(matchQueues);

        int idx = 0;
        while (idx < list.size()-2) {
            list.add(idx+1, list.remove(list.size()-1));
            idx += 2;
        }

        return list;
    }

    public static void main(String[] args)
    {
        List<Integer> list = Arrays.asList(63,87,72,59,50,61);
        System.out.println("unsorted: " + list);

        list.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2)
            {
                return o2.compareTo(o1);
            }
        });
        System.out.println("sorted: " + list);

        BracketSort bs = new BracketSort();
        list = bs.bracketSort(list);
        System.out.println("bracket sorted: " + list);
    }

}
