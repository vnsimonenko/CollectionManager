package vns.collection.syntax;

import org.apache.commons.lang3.StringUtils;
import vns.collection.CollectionException;

/**
 * ColumnNode
 */
public class ColumnNode extends SyntaxNode {
    private String columnName;
    private String tableName;
    private Boolean asc;
    private boolean outerJoin;

    ColumnNode(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isAsc() {
        return asc == null || asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

    public boolean isOuterJoin() {
        return outerJoin;
    }

    public void setOuterJoin(boolean outerJoin) {
        this.outerJoin = outerJoin;
    }

    @Override
    public String check() {
        if (StringUtils.isBlank(columnName)) {
            //TODO CollectionException
            throw new CollectionException("The column name is empty.");
        }
        if (!StringUtils.isBlank(tableName)) {
            SyntaxContext.checkStartName(tableName);
        }
        if (StringUtils.isBlank(tableName)) {
            SyntaxContext.checkStartName(columnName);
        }
        SyntaxContext.checkEndName(columnName);
        StringBuilder sb = new StringBuilder();
        sb.append("[\\s]*");
        if (!StringUtils.isBlank(tableName)) {
            sb.append(tableName);
            sb.append("[.]");
        }
        sb.append(columnName);
        sb.append("[\\s]*");
        if (isOuterJoin()) {
            sb.append("[\\s]*\\[\\+\\][\\s]*");
        }
        if (asc != null) {
            sb.append("[\\s]*");
            sb.append(isAsc() ? "asc" : "desc");
            sb.append("[\\s]*");
        }
        return sb.toString();
    }
}
