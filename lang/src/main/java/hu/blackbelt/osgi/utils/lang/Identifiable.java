package hu.blackbelt.osgi.utils.lang;

import java.io.Serializable;

public interface Identifiable<T extends Serializable> {
    T getId();
}
