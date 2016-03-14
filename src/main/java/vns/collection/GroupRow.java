package vns.collection;

import java.util.HashMap;
import java.util.Map;

/**
 * GroupRow
 */
public class GroupRow {
    private Map<Object, TableRow> tableRowMap = new HashMap<>();

    public boolean addRow(TableRow... tableRows) {
        boolean duplicate = false;
        for (TableRow tableRow : tableRows) {
            if (tableRow.isSurrogate()) {
                continue;
            }
            TableRow testTableRow = tableRowMap.get(tableRow.getTableId());
            if (testTableRow != null) {
                duplicate |= !testTableRow.equals(tableRow);
                continue;
            }
            tableRowMap.put(tableRow.getTableId(), tableRow);
        }
        return duplicate;
    }

    public boolean addRow(GroupRow groupRow) {
        if (groupRow.tableRowMap.size() == 0) {
            return false;
        }
        return addRow(groupRow.tableRowMap.values().toArray(new TableRow[groupRow.tableRowMap.size()]));
    }

    public boolean containsAny(GroupRow groupRow) {
        for (TableRow tableRow : groupRow.tableRowMap.values()) {
            TableRow testTableRow = tableRowMap.get(tableRow.getTableId());
            if (testTableRow != null && testTableRow.equals(tableRow)) {
                return true;
            }
        }
        return false;
    }

    public Map<Object, TableRow> getTableRowMap() {
        return tableRowMap;
    }
}
