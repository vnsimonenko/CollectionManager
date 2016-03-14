package vns.collection;

import java.util.HashMap;
import java.util.Map;

/**
 * Predicate
 */
public enum Predicate {
    LESS("<"), LESS_OR_EQUAL("<="), GREATER(">"), GREATER_OR_EQUAL(">="), EQUAL("="), OR("or"), AND("and");

    Predicate(String key) {
        this.KEY = key;
        setKey(key, this);
    }

    public static Predicate getByKey(String type) {
        return keyMap.get(type);
    }

    private static void setKey(String key, Predicate predicate) {
        if (keyMap == null) {
            keyMap = new HashMap<>();
        }
        keyMap.put(key, predicate);
    }

    public final String KEY;
    private static Map<String, Predicate> keyMap;
}
