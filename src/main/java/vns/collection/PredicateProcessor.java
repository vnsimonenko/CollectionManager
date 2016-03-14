package vns.collection;

import vns.collection.action.Action;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

/**
 * PredicateProcessor
 */
public class PredicateProcessor {
    public List<GroupRow> perform(Action action, List<TableRow> table1, String columnName1, final boolean isOutter1,
                                  List<TableRow> table2, String columnName2, final boolean isOutter2) {
        final boolean isLeftTable1 = table1.size() > table2.size();
        final List<TableRow> leftTable = isLeftTable1 ? table1 : table2;
        final List<TableRow> rightTable = isLeftTable1 ? table2 : table1;

        final Set<TableRow> fromTables1 = isOutter2 ? new HashSet<>(table1) : null;
        final Set<TableRow> fromTables2 = isOutter1 ? new HashSet<>(table2) : null;

        Collection<GroupRow> result = leftTable.parallelStream().map(lval -> {
            Stream<TableRow> stream2 = rightTable.stream().filter(rval -> isLeftTable1
                            ? action.compare(lval.get(columnName1), rval.get(columnName2))
                            : action.compare(rval.get(columnName1), lval.get(columnName2)));
            return new AbstractMap.SimpleEntry(lval, stream2);
        }).flatMap((Function<AbstractMap.SimpleEntry, Stream<GroupRow>>) ent -> {
            TableRow row1 = (TableRow) ent.getKey();
            final Stream s = ((Stream) ent.getValue());
            final Stream.Builder builder = Stream.builder();
            s.forEach(obj -> {
                GroupRow groupRow = new GroupRow();
                TableRow row2 = (TableRow) obj;
                groupRow.addRow(row1, row2);
                builder.add(groupRow);
                if (isOutter1) {
                    fromTables2.remove(isLeftTable1 ? row2 : row1);
                }
                if (isOutter2) {
                    fromTables1.remove(isLeftTable1 ? row1 : row2);
                }
            });
            return builder.build();
        }).collect(Collectors.toCollection((Supplier<Collection<GroupRow>>) () -> isOutter1 || isOutter2
                ? new HashSet<>() : new ArrayList<>()));

        if (isOutter1 && isOutter2) {
            List<GroupRow> groupRows1 = createGroupsForOutter(fromTables1);
            List<GroupRow> groupRows2 = createGroupsForOutter(fromTables2);
            groupRows1.addAll(groupRows2);
            return groupRows1;
        } else if (isOutter1) {
            List<GroupRow> groupRows = createGroupsForOutter(fromTables2);
            groupRows.addAll(result);
            return groupRows;
        } else if (isOutter2) {
            List<GroupRow> groupRows = createGroupsForOutter(fromTables1);
            groupRows.addAll(result);
            return groupRows;
        }

        return result instanceof List ? (List) result : new ArrayList<>(result);
    }

    public List<GroupRow> perform(Action action, List<GroupRow> compositeRows1, List<GroupRow> compositeRows2) {
        final boolean isLeftRows1 = compositeRows1.size() > compositeRows2.size();
        final List<GroupRow> left = isLeftRows1 ? compositeRows1 : compositeRows2;
        final List<GroupRow> right = isLeftRows1 ? compositeRows2 : compositeRows1;
        return left.parallelStream().map(lval -> {
            Stream<GroupRow> stream2 = right.stream().filter(rval ->
                    action.compare(lval, rval));
            return new AbstractMap.SimpleEntry(lval, stream2);
        }).flatMap((Function<AbstractMap.SimpleEntry, Stream<GroupRow>>) ent -> {
            GroupRow row1 = (GroupRow) ent.getKey();
            GroupRow groupRow = new GroupRow();
            groupRow.addRow(row1);
            final Stream s = ((Stream) ent.getValue());
            final Stream.Builder builder = Stream.builder();
            s.forEach(obj -> {
                GroupRow row2 = (GroupRow) obj;
                boolean hasDuplicated = groupRow.addRow(row2);
                if (hasDuplicated) {
                    GroupRow groupRow2 = new GroupRow();
                    groupRow2.addRow(row2);
                    groupRow2.addRow(row1);
                    builder.add(groupRow2);
                }
                builder.add(groupRow);
            });
            return builder.build();
        }).collect(toList());
    }

    public List<GroupRow> perform(List<List<TableRow>> tableRowsList) {
        List<List<GroupRow>> groupRowsList = new ArrayList<>();
        for (List<TableRow> tableRows : tableRowsList) {
            List<GroupRow> groupRows = new ArrayList<>();
            groupRowsList.add(groupRows);
            for (TableRow tableRow : tableRows) {
                GroupRow groupRow = new GroupRow();
                groupRow.addRow(tableRow);
                groupRows.add(groupRow);
            }
        }
        Iterator<List<GroupRow>> it = groupRowsList.iterator();
        List<GroupRow> result = it.next();
        Action act = Action.PREDICATE_ACTION_MAP.get(Predicate.OR);
        while (it.hasNext()) {
            result = perform(act, result, it.next());
        }
        return result;
    }

    private List<GroupRow> createGroupsForOutter(Collection<TableRow> table) {
        return table.stream().map(tableRow -> {
            GroupRow newGroupRow = new GroupRow();
            newGroupRow.addRow(tableRow);
            return newGroupRow;
        }).collect(toCollection((Supplier<List<GroupRow>>) () -> new ArrayList<>()));
    }
}
