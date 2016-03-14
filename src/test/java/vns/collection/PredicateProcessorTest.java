package vns.collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import vns.collection.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * PredicateProcessorTest
 */
public class PredicateProcessorTest {
    private Context context;

    @Before
    public void up() {
        context = new Context();

        TableBuilder builder1 = TableBuilder.createTable();
        for (int i = 0; i < 5; i++) {
            builder1.add()
                    .add("c0", i)
                    .add("c1", i + 1)
                    .add("c2", i + 2)
                    .add("c3", i + 3)
                    .add("c4", i + 4)
                    .add("c5", i + 5).bind();
        }
        context.putTable("table1", TableRow.createTable("table1", builder1.build()));

        TableBuilder builder2 = TableBuilder.createTable();
        for (int i = 0; i < 5; i++) {
            builder2.add()
                    .add("c0", i)
                    .add("c1", i + 1)
                    .add("c2", i + 2)
                    .add("c3", i + 3)
                    .add("c4", i + 4)
                    .add("c5", i + 5).bind();
        }
        context.putTable("table2", TableRow.createTable("table2", builder2.build()));

        TableBuilder builder3 = TableBuilder.createTable();
        for (int i = 0; i < 6; i++) {
            builder3.add()
                    .add("c0", i - 5)
                    .add("c1", i - 4)
                    .add("c2", i - 3)
                    .add("c3", i - 2)
                    .add("c4", i - 1)
                    .add("c5", i).bind();
        }
        context.putTable("table3", TableRow.createTable("table3", builder3.build()));

        List<GroupRow> groupRows = new ArrayList<>();
        GroupRow groupRow = new GroupRow();
        groupRow.addRow(context.getTable("table1").get(0));
        groupRow.addRow(context.getTable("table2").get(0));
        groupRows.add(groupRow);
        groupRow = new GroupRow();
        groupRow.addRow(context.getTable("table1").get(1));
        groupRow.addRow(context.getTable("table2").get(2));
        groupRows.add(groupRow);
        context.putGroup("group1", groupRows);

        groupRows = new ArrayList<>();
        groupRow = new GroupRow();
        groupRow.addRow(context.getTable("table1").get(1));
        groupRow.addRow(context.getTable("table3").get(4));
        groupRows.add(groupRow);
        groupRow = new GroupRow();
        groupRow.addRow(context.getTable("table2").get(2));
        groupRow.addRow(context.getTable("table3").get(4));
        groupRows.add(groupRow);
        context.putGroup("group2", groupRows);
    }

    @Test
    public void performForTable() {
        PredicateProcessor processor = new PredicateProcessor();
        Action.Descriptor descriptor1 = new Action.Descriptor();
        Action.Descriptor descriptor2 = new Action.Descriptor();
        descriptor1.tableRows = context.getTable("table1");
        descriptor1.columnName = "c0";
        descriptor1.outter = false;
        descriptor2.tableRows = context.getTable("table3");
        descriptor2.columnName = "c0";
        descriptor2.outter = false;
        List<GroupRow> groupRows = processor.perform(Action.PREDICATE_ACTION_MAP.get(Predicate.EQUAL),
                descriptor1.tableRows, descriptor1.columnName, descriptor1.outter,
                descriptor2.tableRows, descriptor2.columnName, descriptor2.outter);

        context.putGroup("result", groupRows);

        Set<String> actual = Utils.parseGroups(groupRows);
        Assert.assertTrue(actual.contains("table1:0;table3:5;"));
        Assert.assertEquals(1, actual.size());
    }

    @Test
    public void performForResult() {
        PredicateProcessor processor = new PredicateProcessor();
        List<GroupRow> groupRows = processor.perform(Action.PREDICATE_ACTION_MAP.get(Predicate.AND),
                context.getGroup("group1"), context.getGroup("group2"));
        Assert.assertEquals(2, groupRows.size());

        Set<String> actual = Utils.parseGroups(groupRows);
        Assert.assertTrue(actual.contains("table1:1;table2:2;table3:4;"));
        Assert.assertEquals(1, actual.size());

    }
}
