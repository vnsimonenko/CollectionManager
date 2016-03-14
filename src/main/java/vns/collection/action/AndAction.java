package vns.collection.action;

import vns.collection.Context;
import vns.collection.GroupRow;
import vns.collection.PredicateProcessor;

import java.util.List;

/**
 * AndAction
 */
class AndAction extends Action {

    @Override
    public List<GroupRow> perform(Context context, Descriptor descriptor) {
        PredicateProcessor processor = new PredicateProcessor();
        return processor.perform(this, descriptor.groupRows, descriptor.next.groupRows);
    }

    public boolean compare(Object obj1, Object obj2) {
        GroupRow row1 = (GroupRow) obj1;
        GroupRow row2 = (GroupRow) obj2;
        return row1.containsAny(row2);
    }
}
