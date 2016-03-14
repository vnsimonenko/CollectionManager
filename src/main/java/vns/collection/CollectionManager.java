package vns.collection;

import vns.collection.action.ActionVisitor;
import vns.collection.syntax.ColumnNode;
import vns.collection.syntax.ExpressionParser;
import vns.collection.syntax.SyntaxContext;
import vns.collection.syntax.TableNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Selects data from input tables using sql expression.
 */
public class CollectionManager {
    private String sqlExpression;
    private SyntaxContext syntaxContext;
    private Context context = new Context();

    /**
     * Set sql expression.
     * @param sqlExpression - sql statement.
     */
    public void setSqlExpression(String sqlExpression) {
        this.sqlExpression = sqlExpression;
    }

    /**
     * Set list parameters.
     * @param parameters - list parameters.
     * Can be number or string.
     * Index start from 0 in sql statement.
     */
    public void setParameters(List<Object> parameters) {
        context.setParams(parameters);
    }

    /**
     * Add table for "from" expression.
     * @param name - name table. Name have to match the name of "from" statement.
     * @param table - List<Map<String, Object>> - key in Map is name of column.
     */
    public void addTable(String name, List<Map<String, Object>> table) {
        context.putTable(name, TableRow.createTable(name, table));
    }

    /**
     * It purge list input tables as list parameters given as parameters.
     */
    public void clearTables() {
        context.clearTables();
    }

    /**
     * Formation of the result according to the sql expression and input parameters.
     * @return The list of selected rows of the tables presented in the sql.
     * @throws CollectionException - called when a syntax error and
     * during processing result.
     */
    public List<Map<String, Object>> perform() {
        return perform(true);
    }

    /**
     * @see CollectionManager#perform()
     * @param reCalc - reset state,
     * not recacflculation syntax tree.
     * @return list of row of map instances.
     * @throws CollectionException - called when a syntax error and
     * during processing result.
     */
    public List<Map<String, Object>> perform(boolean reCalc) {
        if (reCalc || syntaxContext == null) {
            syntaxContext = new ExpressionParser().parse(sqlExpression);
        }
        context.clearGroups();
        clearTables();
        List<GroupRow> groupRows = syntaxContext.getPredicateNode() == null ? calculateForFrom() : calculateForWhere();
        List<Map<String, Object>> result = formResultCollection(groupRows);
        if (syntaxContext.getOrderNodes().size() > 0) {
            result = sort(result);
        }
        return result;
    }

    private List<GroupRow> calculateForFrom() {
        List<List<TableRow>> tableRowsList = new ArrayList<>();
        for (TableNode tn : syntaxContext.getTableNodes()) {
            tableRowsList.add(context.getTable(tn.getTableName()));
        }
        return new PredicateProcessor().perform(tableRowsList);
    }

    private List<GroupRow> calculateForWhere() {
        ActionVisitor visitor = new ActionVisitor(context, syntaxContext);
        syntaxContext.getPredicateNode().visit(visitor);
        String root = syntaxContext.getPredicateNode().getName();
        return context.getGroup(root);
    }

    private List<Map<String, Object>> formResultCollection(List<GroupRow> groupRows) {
        List<Map<String, Object>> result = new ArrayList<>();
        Set<Object> uniqRows = syntaxContext.isDistinct() && groupRows.size() > 0 ? new HashSet<>() : null;
        for (GroupRow groupRow : groupRows) {
            Map<Object, TableRow> tableRowMap = groupRow.getTableRowMap();
            Map<String, Object> columns = new HashMap<>();
            for (ColumnNode columnNode : syntaxContext.getSelectNodes()) {
                String tableName = syntaxContext.resolveTableName(columnNode.getTableName());
                TableRow tableRow = tableRowMap.get(tableName);
                columns.put(columnNode.getTableName() + "." + columnNode.getColumnName(),
                        tableRow == null ? null : tableRow.get(columnNode.getColumnName()));
            }
            if (syntaxContext.isDistinct()) {
                if (uniqRows.contains(columns)) {
                    continue;
                }
                uniqRows.add(columns);
            }
            result.add(columns);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> sort(List<Map<String, Object>> table) {
        return table.parallelStream().sorted((row1, row2) -> {
            int compare = 0;
            for (ColumnNode columnNode : syntaxContext.getOrderNodes()) {
                String className = columnNode.getTableName() + "." + columnNode.getColumnName();
                Object val1 = row1.get(className);
                Object val2 = row2.get(className);
                if (val1 instanceof Comparable && val2 instanceof Comparable) {
                    compare = ((Comparable) val1).compareTo(val2);
                    if (compare != 0) {
                        return columnNode.isAsc() ? compare : -compare;
                    }
                }
            }
            return compare;
        }).collect(Collectors.toList());
    }
}
