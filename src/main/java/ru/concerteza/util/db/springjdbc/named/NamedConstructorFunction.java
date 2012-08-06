package ru.concerteza.util.db.springjdbc.named;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nullable;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static ru.concerteza.util.string.CtzFormatUtils.format;

/**
 * Guava function, converts unordered data map into object instance using {@link NamedConstructor}. Designed
 * to use with immutable classes (with final fields) - only constructor invocation is used without any field access.
 * Constructor arguments must be annotated with JSR330 {@link Named} annotations (there are other ways to access
 * constructor names in runtime, see <a href="http://paranamer.codehaus.org/">paranamer project</a>, but we use
 * {@code @Named} annotations only). Constructors without {@code @Named} annotations on arguments will be ignored.
 * Constructors with {@code @Named} annotations must have all they arguments annotated with not blank values without
 * duplicates. All reflection introspection is done on function instantiation. For case insensitive names all
 * {@code @Named} values must be locale insensitive.
 *
 * @param <T> object type to instantiate
 * @author alexey
 * Date: 7/6/12
 * @see NamedConstructor
 * @see NamedConstructorMapper
 */
public class NamedConstructorFunction<T> implements Function<Map<String, ?>, T> {
    private final Map<String, NamedConstructor<T>> constructors;
    private final boolean caseSensitiveNames;

    /**
     * Constructor
     *
     * @param clazz class to introspect and instantiate
     * @param caseSensitiveNames whether too keep case of {@link Named} values
     */
    public NamedConstructorFunction(Class<T> clazz, boolean caseSensitiveNames) {
        checkNotNull(clazz, "Provided class is null");
        this.constructors = buildConstructorsMap(clazz);
        this.caseSensitiveNames = caseSensitiveNames;
    }

    /**
     * Generic-friendly factory method
     *
     * @param clazz class to introspect and instantiate
     * @param caseSensitiveNames whether too keep case of {@link Named} values
     * @param <T> class type
     * @return named constructor function instance
     */
    public static <T> NamedConstructorFunction<T> of(Class<T> clazz, boolean caseSensitiveNames) {
        return new NamedConstructorFunction<T>(clazz, caseSensitiveNames);
    }

    /**
     * Converts unordered data map into object instance using constructor {@link Named} annotated arguments.
     *
     * @param input unordered data map
     * @return instantiated object
     */
    @Override
    public T apply(@Nullable Map<String, ?> input) {
        checkNotNull(input, "Input data map is null");
        NamedConstructor<T> nc = constructors.get(hashNames(input.keySet()));
        checkArgument(null != nc, "No named constructor found for input: '%s', existed constructors: '%s'", input, constructors);
        return nc.invoke(input);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                append("constructors", constructors).
                toString();
    }

    /**
     * Inner use method
     *
     * @return named constructors for inner use
     */
    Map<String, NamedConstructor<T>> getConstructors() {
        return constructors;
    }

    private Map<String, NamedConstructor<T>> buildConstructorsMap(Class<T> clazz) {
        List<NamedConstructor<T>> list = extractNamedConstructors(clazz);
        checkArgument(list.size() > 0, "No named constructors found for class: '%s'", clazz);
        // deliberate manual loop instead of Maps.uniqueIndex for proper error reporting
        Map<String, NamedConstructor<T>> res = Maps.newLinkedHashMap();
        for(NamedConstructor<T> nc : list) {
            String key = hashNames(nc.names);
            NamedConstructor<T> existed = res.put(key, nc);
            if(null != existed) throw new IllegalArgumentException(format(
                    "Named constructors with duplicate names set found, first: '{}', second: '{}'", existed.constructor, nc.constructor));
        }
        return res;
    }

    private List<NamedConstructor<T>> extractNamedConstructors(Class<T> clazz) {
        ImmutableList.Builder<NamedConstructor<T>> builder = ImmutableList.builder();
        for(Constructor<?> co : clazz.getDeclaredConstructors()) {
            Annotation[][] anArray = co.getParameterAnnotations();
            LinkedHashSet<String> names = extractNames(anArray, co.toGenericString());
            if(names.size() > 0) builder.add(new NamedConstructor<T>(co, names));
        }
        return builder.build();
    }

    private LinkedHashSet<String> extractNames(Annotation[][] anArray, String coStr) {
        LinkedHashSet<String> res = Sets.newLinkedHashSet();
        for(Annotation[] anns : anArray) {
            for(Annotation an : anns) {
                if(Named.class.getName().equals(an.annotationType().getName())) {
                    Named na = (Named) an;
                    checkArgument(isNotBlank(na.value()), "@Named annotation with empty value found, constructor: '%s'", coStr);
                    boolean unique = res.add(caseSensitiveNames ? na.value() : na.value().toLowerCase(Locale.ENGLISH));
                    checkArgument(unique, "Not unique @Named value: '%s', constructor: '%s'", na.value(), coStr);
                }
            }
        }
        checkArgument(0 == res.size() || anArray.length == res.size(), "Not consistent @Named annotations found for constructor: '%s'", coStr);
        return res;
    }

    private String hashNames(Collection<String> names) {
        List<String> ordered = Ordering.natural().immutableSortedCopy(names);
        return Joiner.on(", ").join(ordered);
    }
}
