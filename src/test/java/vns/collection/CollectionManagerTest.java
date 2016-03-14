package vns.collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CollectionManagerTest
 */
public class CollectionManagerTest {
    private Map<String, List<Map<String, Object>>> tables;

    @Before
    public void up() {
        tables = new HashMap<>();

        TableBuilder builder = TableBuilder.createTable();
        for (int i = 0; i < 3; i++) {
            builder.add()
                    .add("c0", 0)
                    .add("c1", i)
                    .add("c2", 2 - i).bind();
        }
        tables.put("table1", builder.build());

        builder = TableBuilder.createTable();
        for (int i = 0; i < 3; i++) {
            builder.add()
                    .add("c0", 0)
                    .add("c1", i)
                    .add("c2", 2 - i).bind();
        }
        tables.put("table2", builder.build());

        builder = TableBuilder.createTable();
        for (int i = 0; i < 3; i++) {
            builder.add()
                    .add("c0", 0)
                    .add("c1", i + 2).bind();
        }
        tables.put("table3", builder.build());
    }

    @Test
    public void testSort() {
        List<Object> parameters = new ArrayList<>();
        parameters.add(1);
        parameters.add(2);
        CollectionManager processor = new CollectionManager();
        processor.setSqlExpression("select distinct t1.c1, t2.c1 from table1 t1, table2 t2 where t2.c1 = t1.c1 order by t1.c0 asc, t2.c1 asc");
        processor.setParameters(parameters);
        processor.addTable("table1", tables.get("table1"));
        processor.addTable("table2", tables.get("table2"));
        List<Map<String, Object>> result = processor.perform();
        String s = Utils.parseResultToString(result);
        Assert.assertEquals("t1.c1:0;t2.c1:0;|t1.c1:1;t2.c1:1;|t1.c1:2;t2.c1:2;|", s);

        processor = new CollectionManager();
        processor.setSqlExpression("select distinct t1.c1, t2.c1 from table1 t1, table2 t2 where t2.c1 = t1.c1 order by t1.c0 asc, t2.c1 desc");
        processor.setParameters(parameters);
        processor.addTable("table1", tables.get("table1"));
        processor.addTable("table2", tables.get("table2"));
        result = processor.perform();
        s = Utils.parseResultToString(result);
        Assert.assertEquals("t1.c1:2;t2.c1:2;|t1.c1:1;t2.c1:1;|t1.c1:0;t2.c1:0;|", s);
    }

    @Test
    public void testEqual() {
        List<Object> parameters = new ArrayList<>();
        parameters.add(0);
        parameters.add(1);
        CollectionManager processor = new CollectionManager();
        processor.setSqlExpression("select distinct t1.c1, t2.c1 from table1 t1, table2 t2 where t2.c1 = t1.c1 and (t2.c1 = ?0 or t2.c1 = ?1)");
        processor.setParameters(parameters);
        processor.addTable("table1", tables.get("table1"));
        processor.addTable("table2", tables.get("table2"));
        List<Map<String, Object>> result = processor.perform();
        String s = Utils.parseResultToString(result);
        Assert.assertEquals("t1.c1:0;t2.c1:0;|t1.c1:1;t2.c1:1;|", s);

        processor = new CollectionManager();
        processor.setSqlExpression("select table1.c1, table2.c1 from table1, table2 where table2.c1 = table1.c1 and (table2.c1 = ?0 or table2.c1 = ?1)");
        processor.setParameters(parameters);
        processor.addTable("table1", tables.get("table1"));
        processor.addTable("table2", tables.get("table2"));
        result = processor.perform();
        s = Utils.parseResultToString(result);
        Assert.assertEquals("table1.c1:0;table2.c1:0;|table1.c1:1;table2.c1:1;|", s);

        processor = new CollectionManager();
        processor.setSqlExpression("select t1.c1, t2.c1 from table1 t1, table2 t2 where (((((t2.c1 = t1.c2)))))");
        processor.setParameters(parameters);
        processor.addTable("table1", tables.get("table1"));
        processor.addTable("table2", tables.get("table2"));
        result = processor.perform();
        s = Utils.parseResultToString(result);
        Assert.assertEquals("t1.c1:0;t2.c1:2;|t1.c1:1;t2.c1:1;|t1.c1:2;t2.c1:0;|", s);

        processor = new CollectionManager();
        processor.setSqlExpression("select t1.c1, t1.c2, t2.c1, t2.c2 from table1 t1, table2 t2 where t1.c2 = t2.c1 and (t2.c1 = ?0 or t2.c1 = ?1)");
        processor.setParameters(parameters);
        processor.addTable("table1", tables.get("table1"));
        processor.addTable("table2", tables.get("table2"));
        result = processor.perform();
        s = Utils.parseResultToString(result);
        Assert.assertEquals("t1.c1:2;t1.c2:0;t2.c1:0;t2.c2:2;|t1.c1:1;t1.c2:1;t2.c1:1;t2.c2:1;|", s);
    }

    @Test
    public void testCompare() {
        List<Object> parameters = new ArrayList<>();
        parameters.add(1);
        CollectionManager processor = new CollectionManager();
        processor.setSqlExpression("select t1.c1, t1.c2, t2.c1, t2.c2 from table1 t1, table2 t2 where t1.c1 = t2.c1 and (t1.c1 < ?0)");
        processor.setParameters(parameters);
        processor.addTable("table1", tables.get("table1"));
        processor.addTable("table2", tables.get("table2"));
        List<Map<String, Object>> result = processor.perform();
        String s = Utils.parseResultToString(result);
        Assert.assertEquals("t1.c1:0;t1.c2:2;t2.c1:0;t2.c2:2;|", s);

        processor = new CollectionManager();
        processor.setSqlExpression("select t1.c1, t1.c2, t2.c1, t2.c2 from table1 t1, table2 t2 where t1.c1 = t2.c1 and (t1.c1 <= ?0)");
        processor.setParameters(parameters);
        processor.addTable("table1", tables.get("table1"));
        processor.addTable("table2", tables.get("table2"));
        result = processor.perform();
        s = Utils.parseResultToString(result);
        Assert.assertEquals("t1.c1:0;t1.c2:2;t2.c1:0;t2.c2:2;|t1.c1:1;t1.c2:1;t2.c1:1;t2.c2:1;|", s);

        processor = new CollectionManager();
        processor.setSqlExpression("select t1.c1, t1.c2, t2.c1, t2.c2 from table1 t1, table2 t2 where t1.c1 = t2.c1 and (t1.c1 > ?0)");
        processor.setParameters(parameters);
        processor.addTable("table1", tables.get("table1"));
        processor.addTable("table2", tables.get("table2"));
        result = processor.perform();
        s = Utils.parseResultToString(result);
        Assert.assertEquals("t1.c1:2;t1.c2:0;t2.c1:2;t2.c2:0;|", s);

        processor = new CollectionManager();
        processor.setSqlExpression("select t1.c1, t1.c2, t2.c1, t2.c2 from table1 t1, table2 t2 where t1.c1 = t2.c1 and (t1.c1 >= ?0)");
        processor.setParameters(parameters);
        processor.addTable("table1", tables.get("table1"));
        processor.addTable("table2", tables.get("table2"));
        result = processor.perform();
        s = Utils.parseResultToString(result);
        Assert.assertEquals("t1.c1:1;t1.c2:1;t2.c1:1;t2.c2:1;|t1.c1:2;t1.c2:0;t2.c1:2;t2.c2:0;|", s);
    }

    @Test
    public void testOutter() {
        CollectionManager processor = new CollectionManager();
        processor.setSqlExpression("select t1.c1, t2.c1 from table1 t1, table3 t2 where t1.c1 = t2.c1[+]");
        processor.addTable("table1", tables.get("table1"));
        processor.addTable("table3", tables.get("table3"));
        List<Map<String, Object>> result = processor.perform();
        String s = Utils.parseResultToString(result);
        Assert.assertEquals("t1.c1:0;t2.c1:null;|t1.c1:1;t2.c1:null;|t1.c1:2;t2.c1:2;|", s);

        processor = new CollectionManager();
        processor.setSqlExpression("select t1.c1, t2.c1 from table1 t1, table3 t2 where t1.c1[+] = t2.c1");
        processor.addTable("table1", tables.get("table1"));
        processor.addTable("table3", tables.get("table3"));
        result = processor.perform();
        s = Utils.parseResultToString(result);
        Assert.assertEquals("t1.c1:null;t2.c1:4;|t1.c1:null;t2.c1:3;|t1.c1:2;t2.c1:2;|", s);

        processor = new CollectionManager();
        processor.setSqlExpression("select t1.c1, t2.c1 from table1 t1, table3 t2 where t1.c1[+] = t2.c1[+]");
        processor.addTable("table1", tables.get("table1"));
        processor.addTable("table3", tables.get("table3"));
        result = processor.perform();
        s = Utils.parseResultToString(result);
        Assert.assertEquals("t1.c1:0;t2.c1:null;|t1.c1:1;t2.c1:null;|t1.c1:null;t2.c1:4;|t1.c1:null;t2.c1:3;|", s);
    }

    @Test
    public void testFrom() {
        CollectionManager processor = new CollectionManager();
        processor.setSqlExpression("select table1.c1 from table1");
        processor.addTable("table1", tables.get("table1"));
        List<Map<String, Object>> result = processor.perform();
        String s = Utils.parseResultToString(result);
        Assert.assertEquals("table1.c1:0;|table1.c1:1;|table1.c1:2;|", s);

        processor = new CollectionManager();
        processor.setSqlExpression("select table1.c1, table2.c1 from table1, table2");
        processor.addTable("table1", tables.get("table1"));
        processor.addTable("table2", tables.get("table2"));
        result = processor.perform();
        s = Utils.parseResultToString(result);
        Assert.assertEquals(
                "table1.c1:0;table2.c1:0;|" +
                "table1.c1:1;table2.c1:0;|" +
                "table1.c1:2;table2.c1:0;|" +
                "table1.c1:0;table2.c1:1;|" +
                "table1.c1:1;table2.c1:1;|" +
                "table1.c1:2;table2.c1:1;|" +
                "table1.c1:0;table2.c1:2;|" +
                "table1.c1:1;table2.c1:2;|" +
                "table1.c1:2;table2.c1:2;|", s);
    }
}
