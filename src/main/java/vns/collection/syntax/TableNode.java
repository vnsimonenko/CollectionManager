package vns.collection.syntax;

import org.apache.commons.lang3.StringUtils;
import vns.collection.CollectionException;

/**
 * TableNode
 */
public class TableNode extends SyntaxNode {
    private String tableName;
    private String aliasTableName;

    TableNode(String tableName, String shortTableName) {
        this.tableName = tableName;
        this.aliasTableName = shortTableName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getAliasTableName() {
        return aliasTableName;
    }

    @Override
    public String check() {
        if (StringUtils.isBlank(tableName)) {
            //TODO CollectionException
            throw new CollectionException("The table name is empty.");
        }
        SyntaxContext.checkStartName(tableName);
        SyntaxContext.checkEndName(tableName);

        if (!StringUtils.isBlank(aliasTableName)) {
            SyntaxContext.checkStartName(aliasTableName);
            SyntaxContext.checkEndName(aliasTableName);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[\\s]*");
        sb.append(tableName);
        sb.append("[\\s]*");
        if (!StringUtils.isBlank(aliasTableName)) {
            sb.append("[\\s]*");
            sb.append(aliasTableName);
            sb.append("[\\s]*");
        }
        return sb.toString();
    }
}
