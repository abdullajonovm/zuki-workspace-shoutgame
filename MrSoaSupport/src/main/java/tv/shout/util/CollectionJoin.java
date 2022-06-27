package tv.shout.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class CollectionJoin {
    public static <E> String join(Collection<E> collection, String delimiter, E...skip) {
        Set<E> skipSet = new HashSet<E>(Arrays.asList(skip));
        StringBuffer result = new StringBuffer();
        Iterator<E> it = collection.iterator();
        boolean first = true;
        while (it.hasNext()) {
            E n = it.next();
            if (!skipSet.contains(n)) {
                if (!first)
                    result.append(delimiter);
                else
                    first = false;
                result.append(n);
            }
        }
        return result.toString();
    }
}
