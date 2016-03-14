package vns.collection.action;

import org.apache.commons.lang3.StringUtils;
import vns.collection.Context;
import vns.collection.GroupRow;
import vns.collection.Predicate;
import vns.collection.TableRow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Action
 */
public abstract class Action {
    public static Map<Predicate, Action> PREDICATE_ACTION_MAP = new HashMap<>();

    public static final class Descriptor {
        public Descriptor next;
        public List<TableRow> tableRows;
        public List<GroupRow> groupRows;
        public String columnName = StringUtils.EMPTY;
        public boolean outter;
    }

    static {
        PREDICATE_ACTION_MAP.put(Predicate.EQUAL, new EqualAction());
        PREDICATE_ACTION_MAP.put(Predicate.GREATER, new GreaterAction());
        PREDICATE_ACTION_MAP.put(Predicate.GREATER_OR_EQUAL, new GreaterOrEqualAction());
        PREDICATE_ACTION_MAP.put(Predicate.LESS, new LessAction());
        PREDICATE_ACTION_MAP.put(Predicate.LESS_OR_EQUAL, new LessOrEqualAction());
        PREDICATE_ACTION_MAP.put(Predicate.OR, new OrAction());
        PREDICATE_ACTION_MAP.put(Predicate.AND, new AndAction());
    }

    public abstract List<GroupRow> perform(Context context, Descriptor descriptor);

    public abstract boolean compare(Object obj1, Object obj2);
}
