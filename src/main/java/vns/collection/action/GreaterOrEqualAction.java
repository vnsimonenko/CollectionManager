package vns.collection.action;

import org.apache.commons.lang3.ObjectUtils;

/**
 * GreaterOrEqualAction
 */
class GreaterOrEqualAction extends EqualAction {
    public boolean compare(Object obj1, Object obj2) {
        return obj1 == null || obj2 == null ? false : ObjectUtils.compare((Comparable) obj1, (Comparable) obj2) >= 0;
    }
}

