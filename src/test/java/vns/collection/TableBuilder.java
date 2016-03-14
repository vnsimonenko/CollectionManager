package vns.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TableBuilder
 */
public class TableBuilder {
    private List<Row> rows = new ArrayList<>();

    private TableBuilder() {
    }

    public static TableBuilder createTable() {
        return new TableBuilder();
    }

    public Row add() {
        return new Row();
    }

    public List<Map<String, Object>> build() {
        List<Map<String, Object>> table = new ArrayList<>();
        for (Row row : rows) {
            table.add(row.columns);
        }
        return table;
    }

    public class Row {
        Map<String, Object> columns = new HashMap<>();

        public Row add(String name, Object value) {
            columns.put(name, value);
            return this;
        }

        public TableBuilder bind() {
            TableBuilder.this.rows.add(this);
            return TableBuilder.this;
        }
    }
}
