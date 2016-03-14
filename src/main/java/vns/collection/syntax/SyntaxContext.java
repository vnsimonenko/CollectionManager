package vns.collection.syntax;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

/**
 * SyntaxContext
 */
public class SyntaxContext {
    private PredicateNode predicateNode;
    private List<ColumnNode> selectNodes;
    private List<TableNode> tableNodes;
    private List<ColumnNode> orderNodes = Collections.emptyList();
    private Map<String, TableNode> aliasTableName = new HashMap<>();
    private Map<String, TableNode> aliasShortTableName = new HashMap<>();
    private TableNode defaultTableNode;
    private boolean distinct;

    public PredicateNode getPredicateNode() {
        return predicateNode;
    }

    public void setPredicateNode(PredicateNode predicateNode) {
        this.predicateNode = predicateNode;
    }

    public void setTableNodes(List<TableNode> tableNodes) {
        this.tableNodes = tableNodes;
        if (tableNodes.isEmpty()) {
            throw new IllegalArgumentException("Table nodes must be not empty");
        }
        for (TableNode t : tableNodes) {
            if (defaultTableNode == null) {
                defaultTableNode = t;
            }
            if (!StringUtils.isBlank(t.getTableName())) {
                aliasTableName.put(t.getTableName(), t);
            }
            if (!StringUtils.isBlank(t.getAliasTableName())) {
                aliasShortTableName.put(t.getAliasTableName(), t);
            }
        }
    }

    public List<TableNode> getTableNodes() {
        return Collections.unmodifiableList(tableNodes);
    }

    public void setSelectNodes(List<ColumnNode> columnNodes) {
        if (columnNodes.isEmpty()) {
            throw new IllegalArgumentException("Column nodes must be not empty");
        }
        this.selectNodes = columnNodes;
    }

    public List<ColumnNode> getSelectNodes() {
        return selectNodes;
    }

    public String resolveTableName(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            return defaultTableNode.getTableName();
        }
        TableNode tableNode = aliasTableName.get(tableName);
        if (tableNode == null) {
            tableNode = aliasShortTableName.get(tableName);
        }
        return tableNode.getTableName();
    }

    static PredicateNode createPredicateNode() {
        return new PredicateNode();
    }

    static TableNode createTableNode(String tableName, String shortTableName) {
        return new TableNode(tableName, shortTableName);
    }

    static ColumnNode createColumnNode(String tableName, String columnName) {
        return new ColumnNode(tableName, columnName);
    }

    static ParameterNode createParamNode(String name) {
        if (!name.matches("^[\\d]+$")) {
            throw new PatternSyntaxException("The name of parameter isn't correct", name, 0);
        }
        return new ParameterNode(name);
    }

    public void setOrderNodes(List<ColumnNode> orderNodes) {
        this.orderNodes = orderNodes;
    }

    public List<ColumnNode> getOrderNodes() {
        return orderNodes;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    static void checkStartName(String name) {
        if (name.matches("^(\\s+|[^a-z_A-Z]+).*")) {
            throw new PatternSyntaxException("The name isn't correct", "", 0);
        }
    }

    static void checkEndName(String name) {
        if (name.matches(".*(\\s+|[^a-z_A-Z0-9]+)$")) {
            throw new PatternSyntaxException("The name isn't correct", "", 0);
        }
    }
}
