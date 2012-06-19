package ru.concerteza.util.collection;

import com.google.common.base.Function;
import com.google.common.collect.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Collection utilities
 *
 * @author alexey
 * Date: 6/9/11
 */
public class CtzCollectionUtils {
    @Deprecated // use ImmutableMap.of() directly
    public static final Map<String, Object> EMPTY_MAP = ImmutableMap.of();

    /**
     * Fires guava transform chain for iterators, which may produce nullable elements
     *
     * @param iter input iterator
     * @return count of transformed elements
     */

    public static long fireTransform(Iterator<?> iter) {
        int counter = 0;
        while (iter.hasNext()) {
            iter.next();
            counter +=1;
        }
        return counter;
    }

    /**
     * Fires guava transform chain for iterables, which may produce nullable elements
     *
     * @param iter input iterable
     * @return count of transformed elements
     */
    public static long fireTransform(Iterable<?> iter) {
        return fireTransform(iter.iterator());
    }

    /**
     * Produces map from key set, values are function products. Inspired by
     * <a href="http://docs.guava-libraries.googlecode.com/git-history/v12.0/javadoc/com/google/common/collect/Maps.html#uniqueIndex%28java.util.Iterator,%20com.google.common.base.Function%29">uniqueIndex</a> method
     *
     * @param keySet input set, contains maps keys
     * @param valueFunction produces map values based on set keys
     * @param <K> map key type
     * @param <V> map value type
     * @return map based on input set, values are function products
     */
    public static <K, V> ImmutableMap<K, V> keySetToMap(Set<K> keySet, Function<? super K, V> valueFunction) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for(K key : keySet) {
            builder.put(key, valueFunction.apply(key));
        }
        return builder.build();
    }
}
