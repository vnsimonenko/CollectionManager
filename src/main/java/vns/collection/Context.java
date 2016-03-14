package vns.collection;

import java.util.*;

/**
 * Context
 */
public class Context {
    private List<Object> params = new ArrayList<>();
    private Map<String, List<TableRow>> tables = new HashMap<>();
    private Map<String, List<GroupRow>> groups = new HashMap<>();

    public List<GroupRow> getGroup(String name) {
        return Collections.unmodifiableList(groups.get(name));
    }

    public List<TableRow> getTable(String name) {
        return Collections.unmodifiableList(tables.get(name));
    }

    public void putTable(String name, List<TableRow> table) {
        this.tables.put(name, table);
    }

    public void clearTables() {
        tables.clear();
    }

    public void clearGroups() {
        groups.clear();
    }

    public void putGroup(String name, List<GroupRow> group) {
        this.groups.put(name, group);
    }

    public List<Object> getParams() {
        return Collections.unmodifiableList(params);
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }
}
