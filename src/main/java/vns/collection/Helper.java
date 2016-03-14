package vns.collection;

import java.util.Arrays;
import java.util.Map;

/**
 * Helper
 */
public class Helper {
    public static int getHashCode(Object... objs) {
        return Arrays.asList(objs).hashCode();
    }

    public interface ValueFactory<T> {
        T create();
    }

    public static <T, K1> T getMultiplicityValueFromMap(Map<K1, T> map, K1 key, ValueFactory<T> factory) {
        T val = map.get(key);
        if (val == null) {
            val = factory.create();
            map.put(key, val);
            return val;
        }
        return val;
    }

    public static String normalize(String in) {
        return in.trim();
    }
}
