package vns.collection;

import java.util.*;

/**
 * Utils
 */
public class Utils {
    public static LinkedHashSet<String> parseGroups(Collection<GroupRow> groupRows) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (GroupRow groupRow : groupRows) {
            List<String> strings = new ArrayList<>();
            for (TableRow tableRow : groupRow.getTableRowMap().values()) {
                strings.add(tableRow.getTableId() + ":" + tableRow.getId());
            }
            strings.sort((o1, o2) -> o1.compareTo(o2));
            String id = "";
            for (String s : strings) {
                id += s + ";";
            }
            result.add(id);
        }
        return result;
    }

    public static LinkedHashSet<String> parseResult(List<Map<String, Object>> table) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (Map<String, Object> row : table) {
            List<String> strings = new ArrayList<>();
            for (Map.Entry<String, Object> ent : row.entrySet()) {
                strings.add(ent.getKey() + ":" + ent.getValue());
            }
            strings.sort((o1, o2) -> o1.compareTo(o2));
            String id = "";
            for (String s : strings) {
                id += s + ";";
            }
            result.add(id);
        }
        return result;
    }

    public static String parseResultToString(List<Map<String, Object>> table) {
        String result = "";
        for (String s : parseResult(table)) {
            result += s + "|";
        }
        return result;
    }
}
