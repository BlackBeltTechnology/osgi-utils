package hu.blackbelt.osgi.utils.lang;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

/**
 * Iterable which orders the elements by the supplied rank parameter ({@link #add(Object, Integer)}.
 * The {@link #iterator()} returns the elements ordered by rank in descending order.
 * 
 * @param <T> the type of elements returned by the iterator
 */
public class RankOrderedIterable<T> implements Iterable<T> {
    public static final long serialVersionUID = 1L;
    private static final int DEFAULT_RANK = 0;
    
    private Set<RankedObject<T>> set;

    /**
     * Create a new empty instance.
     */
    public RankOrderedIterable() {
        set = new ConcurrentSkipListSet<RankOrderedIterable.RankedObject<T>>();
    }

    /**
     * Creates a new empty instance.
     * 
     * @return new instance
     */
    public static <T> RankOrderedIterable<T> createRankOrderedIterable() {
        return new RankOrderedIterable<T>();
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.transform(set.iterator(), new Function<RankedObject<T>, T>() {

            @Override
            public T apply(RankedObject<T> input) {
                return input.object;
            }
        });
    }
    
    /**
     * Adds an object to the iterable, the position will be determined using the supplied rank.
     * 
     * @param object object
     * @param rank rank (defaults to 0 if null)
     */
    public void add(T object, Integer rank) {
        checkNotNull(object);
        RankedObject<T> rankedObject = new RankedObject<T>(object, firstNonNull(rank, DEFAULT_RANK));
        set.add(rankedObject);
    }
    
    /**
     * Removes the object from the iterable.
     * 
     * @param object object
     * @param rank rank (defaults to 0 if null)
     */
    public void remove(T object, Integer rank) {
        checkNotNull(object);
        set.remove(new RankedObject<T>(object, firstNonNull(rank, DEFAULT_RANK)));
    }
    
    /**
     * Wrapper for the rank-object pair.
     * 
     * @param <T> object's type
     */
    @AllArgsConstructor
    @EqualsAndHashCode
    private static final class RankedObject<T> implements Comparable<RankedObject<T>> {
        private T object;
        private int rank;
        
        @Override
        public int compareTo(RankedObject<T> other) {
            int result = other.rank - this.rank;
            if (result == 0) {
                result = other.object.getClass().getName().compareTo(this.object.getClass().getName());
            }
            return result;
        }
    }

}
