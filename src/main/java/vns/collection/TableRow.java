package vns.collection;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

/**
 * TableRow
 */
public class TableRow {
    private int id;
    private Object tableId;
    private int hash;
    private Map<String, Object> columnMap = new HashMap<>();
    private boolean surrogate = false;

    private TableRow(Object tableId, int id, Map<String, Object> columns) {
        this.id = id;
        this.tableId = tableId;
        this.columnMap.putAll(columns);
        hash = new HashCodeBuilder().append(tableId).append(id).build();
    }

    private TableRow(Object value) {
        this.id = 0;
        this.columnMap.put("", value);
        this.tableId = new Object();
        this.surrogate = true;
        hash = new HashCodeBuilder().append(tableId).append(id).build();
    }

    public Object get(String name) {
        return columnMap.get(name);
    }

    public Object getTableId() {
        return tableId;
    }

    public int getId() {
        return id;
    }

    public boolean isSurrogate() {
        return surrogate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TableRow)) {
            return false;
        }
        TableRow tableRow = (TableRow) o;
        if (id != tableRow.id) {
            return false;
        }
        return getTableId() != null ? getTableId().equals(tableRow.getTableId()) : false;

    }

    @Override
    public int hashCode() {
        return hash;
    }

    public static List<TableRow> createTable(Object tableId, List<Map<String, Object>> table) {
        List<TableRow> tableRows = new ArrayList<>();
        int i = 0;
        for (Map<String, Object> columns : table) {
            TableRow row = new TableRow(tableId, i++, columns);
            tableRows.add(row);
        }
        return tableRows;
    }

    public static List<TableRow> createTable(Object value) {
        return Arrays.asList(new TableRow(value));
    }
}