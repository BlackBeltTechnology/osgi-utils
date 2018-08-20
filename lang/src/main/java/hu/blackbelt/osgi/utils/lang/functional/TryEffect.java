package hu.blackbelt.osgi.utils.lang.functional;

public interface TryEffect<A extends Exception> {
    void apply() throws A;
}
