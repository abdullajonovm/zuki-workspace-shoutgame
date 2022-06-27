package tv.shout.util;

import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StreamUtil
{
    public static <T> Collector<T, ?, Optional<T>> singleOrEmpty() {
        return Collectors.collectingAndThen(Collectors.toSet(), set -> set.size() == 1 ? set.stream().findAny() : Optional.empty());
    }

}
