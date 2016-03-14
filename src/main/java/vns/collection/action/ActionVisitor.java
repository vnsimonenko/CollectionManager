package vns.collection.action;

import vns.collection.Context;
import vns.collection.TableRow;
import vns.collection.syntax.*;

import java.util.Stack;

/**
 * ActionVisitor
 */
public class ActionVisitor implements SyntaxVisitor {
    private Context context;
    private SyntaxContext syntaxContext;
    private Stack<Action.Descriptor> parameters = new Stack<>();

    public ActionVisitor(Context context, SyntaxContext syntaxContext) {
        this.context = context;
        this.syntaxContext = syntaxContext;
    }

    public void perform(SyntaxNode node) {
        if (node instanceof ColumnNode) {
            performBy((ColumnNode) node);
        } else if (node instanceof ParameterNode) {
            performBy((ParameterNode) node);
        } else if (node instanceof PredicateNode) {
            performBy((PredicateNode) node);
        }
    }

    private void performBy(ParameterNode node) {
        Object value = context.getParams().get(node.getIndex());
        Action.Descriptor descriptor = new Action.Descriptor();
        descriptor.tableRows = TableRow.createTable(value);
        parameters.push(descriptor);
    }

    private void performBy(ColumnNode node) {
        String tableName = syntaxContext.resolveTableName(node.getTableName());
        Action.Descriptor descriptor = new Action.Descriptor();
        descriptor.columnName = node.getColumnName();
        descriptor.tableRows = context.getTable(tableName);
        descriptor.outter = node.isOuterJoin();
        parameters.push(descriptor);
    }

    private void performBy(PredicateNode node) {
        Action action = Action.PREDICATE_ACTION_MAP.get(node.getPredicate());
        Action.Descriptor right = parameters.pop();
        Action.Descriptor left = parameters.pop();
        left.next = right;
        Action.Descriptor descriptor = new Action.Descriptor();
        descriptor.groupRows = action.perform(context, left);
        context.putGroup(node.getName(), descriptor.groupRows);
        parameters.add(descriptor);
    }
}
