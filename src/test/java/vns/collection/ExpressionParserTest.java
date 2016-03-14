package vns.collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import vns.collection.syntax.*;

import java.util.HashSet;
import java.util.Set;

/**
 * ExpressionParserTest
 */
public class ExpressionParserTest {

    @Before
    public void up() {
    }

    @Test
    public void testWhere() {
        ExpressionParser parser = new ExpressionParser();
        SyntaxContext syntaxContext = parser.parse("select c1 from table1 where t1.c1 = t2.c2[+]");
        VisitorTest visitorTest = new VisitorTest();
        syntaxContext.getPredicateNode().visit(visitorTest);
        Assert.assertTrue(visitorTest.getExpressions().contains("@0:t1.c1 = t2.c2[+]:EQUAL"));
        Assert.assertEquals(1, visitorTest.getExpressions().size());

        syntaxContext = parser.parse("select c1 from table1 where t1.c1[+] = t2.c2");
        visitorTest = new VisitorTest();
        syntaxContext.getPredicateNode().visit(visitorTest);
        Assert.assertTrue(visitorTest.getExpressions().contains("@0:t1.c1[+] = t2.c2:EQUAL"));
        Assert.assertEquals(1, visitorTest.getExpressions().size());

        syntaxContext = parser.parse("select c1 from table1 where t1.c1 < t2.c2 or t1.c1 <= t2.c2 and t1.c1 > t2.c2 and t1.c1 >= t2.c2");
        visitorTest = new VisitorTest();
        syntaxContext.getPredicateNode().visit(visitorTest);
        Assert.assertTrue(visitorTest.getExpressions().contains("@5:t1.c1 > t2.c2:GREATER"));
        Assert.assertTrue(visitorTest.getExpressions().contains("@6:t1.c1 >= t2.c2:GREATER_OR_EQUAL"));
        Assert.assertTrue(visitorTest.getExpressions().contains("@1:@2 and @5:AND"));
        Assert.assertTrue(visitorTest.getExpressions().contains("@2:@3 or @4:OR"));
        Assert.assertTrue(visitorTest.getExpressions().contains("@0:@1 and @6:AND"));
        Assert.assertTrue(visitorTest.getExpressions().contains("@4:t1.c1 <= t2.c2:LESS_OR_EQUAL"));
        Assert.assertTrue(visitorTest.getExpressions().contains("@3:t1.c1 < t2.c2:LESS"));
        Assert.assertEquals(7, visitorTest.getExpressions().size());

        syntaxContext = parser.parse("select t1.c1 from table1 t1, table2 t2 where (((t1.c1 = ?0) and (t1.c1 = t2.c2 or t1.c1 = ?1)) or t2.c2 = ?1) and t1.c1 = ?2");
        visitorTest = new VisitorTest();
        syntaxContext.getPredicateNode().visit(visitorTest);
        Assert.assertTrue(visitorTest.getExpressions().contains("@0:t1.c1 = ?0:EQUAL"));
        Assert.assertTrue(visitorTest.getExpressions().contains("@2:t1.c1 = t2.c2:EQUAL"));
        Assert.assertTrue(visitorTest.getExpressions().contains("@3:t1.c1 = ?1:EQUAL"));
        Assert.assertTrue(visitorTest.getExpressions().contains("@1:@2 or @3:OR"));
        Assert.assertTrue(visitorTest.getExpressions().contains("@4:@0 and @1:AND"));
        Assert.assertTrue(visitorTest.getExpressions().contains("@6:t2.c2 = ?1:EQUAL"));
        Assert.assertTrue(visitorTest.getExpressions().contains("@5:@4 or @6:OR"));
        Assert.assertTrue(visitorTest.getExpressions().contains("@8:t1.c1 = ?2:EQUAL"));
        Assert.assertTrue(visitorTest.getExpressions().contains("@7:@5 and @8:AND"));
        Assert.assertEquals(9, visitorTest.getExpressions().size());

        parser.parse("select c1 from table1 where t1.c1 = ?2");
        try {
            parser.parse("select c1 from table1 where t1.c1 == ?2");
            Assert.fail();
        } catch (Exception ex) {}

        parser.parse("select c1 from table1 where t1.c1 = ?2");
        try {
            parser.parse("select c1 from table1 where t1.c1 =< ?2");
            Assert.fail();
        } catch (Exception ex) {}

        parser.parse("select c1 from table1 where t1.c1<?2");
        try {
            parser.parse("select c1 from table1 where t1.c1<<?2");
            Assert.fail();
        } catch (Exception ex) {}

        parser.parse("select c1 from table1 where t1.c1 >?2");
        try {
            parser.parse("select c1 from table1 where t1.c1 >>?2");
            Assert.fail();
        } catch (Exception ex) {}

        parser.parse("select c1 from table1 where t1.c1 >=?2");
        try {
            parser.parse("select c1 from table1 where t1.c1 >>=?2");
            Assert.fail();
        } catch (Exception ex) {}

        parser.parse("select c1 from table1 where (c1 = ?1) and (c2 = ?2)");
        parser.parse("select c1 from table1 where (c1 = ?2)");
        try {
            parser.parse("select c1 from table1 where (c1 and ?2)");
            Assert.fail();
        } catch (Exception ex) {}

        parser.parse("select c1 from table1 where c1 = ?1 or c2=?2");
        parser.parse("select c1 from table1 where c1=?2");
        try {
            parser.parse("select c1 from table1 where c1 or ?2");
            Assert.fail();
        } catch (Exception ex) {}

        try {
            parser.parse("select c1 from table1 where (c1=?2");
            Assert.fail();
        } catch (Exception ex) {}
    }

    @Test
    public void parseSelect() {
        ExpressionParser parser = new ExpressionParser();
        SyntaxContext syntaxContext = parser.parse("select c1, t1.c1 from table1");
        ColumnNode cn = syntaxContext.getSelectNodes().get(0);
        Assert.assertEquals("c1", cn.getColumnName());
        Assert.assertEquals("", cn.getTableName());

        cn = syntaxContext.getSelectNodes().get(1);
        Assert.assertEquals("c1", cn.getColumnName());
        Assert.assertEquals("t1", cn.getTableName());

        try {
            parser.parse("select .c1 from table1");
            Assert.fail();
        } catch (Exception ex) {}

        try {
            parser.parse("select c1. from table1");
            Assert.fail();
        } catch (Exception ex) {}

        parser.parse("select t1.c1 from table1");
        try {
            parser.parse("select .t1.c1 from table1");
            Assert.fail();
        } catch (Exception ex) {}

        parser.parse("select c1, t1.c1 from table1");
        try {
            parser.parse("select c1, t1.c1. from table1");
            Assert.fail();
        } catch (Exception ex) {}
    }

    @Test
    public void parseTable() {
        ExpressionParser parser = new ExpressionParser();
        SyntaxContext syntaxContext = parser.parse("select c1 from table1, table2 t2");
        TableNode tn = syntaxContext.getTableNodes().get(0);
        Assert.assertEquals("table1", tn.getTableName());
        Assert.assertEquals("", tn.getAliasTableName());
        tn = syntaxContext.getTableNodes().get(1);
        Assert.assertEquals("table2", tn.getTableName());
        Assert.assertEquals("t2", tn.getAliasTableName());

        parser.parse("select c1 from table1");
        try {
            parser.parse("select c1 from .table1");
            Assert.fail();
        } catch (Exception ex) {}

        parser.parse("select c1 from table1, table2");
        try {
            parser.parse("select c1 from table1., table2");
            Assert.fail();
        } catch (Exception ex) {}
    }

    @Test
    public void parseOutterForPredicate() {
        ExpressionParser parser = new ExpressionParser();
        parser.parse("select c1 from table1 where (( t1[+] = ?0 ) or (t2=?1))");
        parser.parse("select c1 from table1 where (( t1 = ?0 ) or (?1=t2[+]))");
        parser.parse("select c1 from table1 where (( t1[+] = t2[+] ) or (t2=?1))");
    }

    class VisitorTest implements SyntaxVisitor {
        private Set<String> expressions = new HashSet<>();

        public void perform(SyntaxNode node) {
            if (node instanceof PredicateNode) {
                PredicateNode predicateNode = (PredicateNode) node;
                expressions.add(predicateNode.getName() + ":" + predicateNode.getExpression() + ":" + predicateNode.getPredicate());
            }
        }

        public Set<String> getExpressions() {
            return expressions;
        }
    }
}
