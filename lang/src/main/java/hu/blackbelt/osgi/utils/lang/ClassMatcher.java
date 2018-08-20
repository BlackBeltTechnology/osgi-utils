package hu.blackbelt.osgi.utils.lang;

public interface ClassMatcher {
    <A> boolean isMatch(Object key, Class<A> clazz);
}
