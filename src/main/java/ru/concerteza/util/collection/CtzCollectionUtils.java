package ru.concerteza.util.collection;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: alexey
 * Date: 6/9/11
 */
public class CtzCollectionUtils {
    public static final Map<String, Object> EMPTY_MAP = ImmutableMap.of();

    // fire transform chain for iters with nullable elements
    public static <T> void fireTransform(Iterator<T> iter) {
        while (iter.hasNext()) iter.next();
    }

    public static <T> void fireTransform(Iterable<T> iter) {
        fireTransform(iter.iterator());
    }
}