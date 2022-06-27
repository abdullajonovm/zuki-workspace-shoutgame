package tv.shout.snowyowl.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.meinc.gameplay.domain.Tuple;

public abstract class LocalizationHelper
{
    protected static <T> Map<T, T> tupleListToMap(List<Tuple<T>> list)
    {
        if (list == null) return null;

        Map<T, T> map = new HashMap<>(list.size());
        list.forEach(tuple -> {
            map.put(tuple.getKey(), tuple.getVal());
        });

        return map;
    }

}
