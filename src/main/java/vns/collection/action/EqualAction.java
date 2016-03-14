package vns.collection.action;

import vns.collection.Context;
import vns.collection.GroupRow;
import vns.collection.PredicateProcessor;

import java.util.List;
import java.util.Objects;

/**
 * EqualAction
 */
class EqualAction extends Action {
    @Override
    public List<GroupRow> perform(Context context, Descriptor descriptor) {
        PredicateProcessor processor = new PredicateProcessor();

        Descriptor d1 = descriptor;
        Descriptor d2 = descriptor.next;

        return processor.perform(this, d1.tableRows, d1.columnName, d1.outter,
                d2.tableRows, d2.columnName, d2.outter);
    }

    public boolean compare(Object obj1, Object obj2) {
        return obj1 == null || obj2 == null ? false : Objects.equals(obj1, obj2);
    }
}
