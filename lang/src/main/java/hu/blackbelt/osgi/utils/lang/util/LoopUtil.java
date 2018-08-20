package hu.blackbelt.osgi.utils.lang.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class LoopUtil {
    private LoopUtil() {
    }

    public static <T, R> List<R> loopAndCollect(List<T> elements, Function<T, R> function) {
        return elements.stream()
                .map(function)
                .filter(o -> o != null)
                .collect(Collectors.toList());
    }
}
