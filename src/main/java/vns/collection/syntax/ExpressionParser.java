package vns.collection.syntax;

import org.apache.commons.lang3.StringUtils;
import vns.collection.CollectionException;
import vns.collection.Helper;
import vns.collection.Predicate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ExpressionParser
 */
public class ExpressionParser {
    private int count;
    private Map<String, PredicateNode> predicateNodes;
    private boolean distinct;
    private final static Map<String, String> actionOrder = new HashMap();

    static {
        actionOrder.put(Predicate.AND.KEY, Predicate.OR.KEY);
        actionOrder.put(Predicate.OR.KEY, Predicate.LESS_OR_EQUAL.KEY);
        actionOrder.put(Predicate.LESS_OR_EQUAL.KEY, Predicate.GREATER_OR_EQUAL.KEY);
        actionOrder.put(Predicate.GREATER_OR_EQUAL.KEY, Predicate.LESS.KEY);
        actionOrder.put(Predicate.LESS.KEY, Predicate.GREATER.KEY);
        actionOrder.put(Predicate.GREATER.KEY, Predicate.EQUAL.KEY);
        actionOrder.put(Predicate.EQUAL.KEY, null);
    }

    public SyntaxContext parse(String expression) {
        count = 0;
        distinct = false;
        SyntaxContext syntaxContext = new SyntaxContext();
        predicateNodes = new HashMap<>();
        Map<String, String> lex = parseSqlLex(Helper.normalize(expression));
        if (lex.containsKey("select")) {
            syntaxContext.setSelectNodes(parseSelectNode(lex.get("select")));
        }
        if (lex.containsKey("from")) {
            syntaxContext.setTableNodes(parseFromNode(lex.get("from")));
        }
        if (lex.containsKey("where")) {
            String root = parsePredicateNode(lex.get("where"));
            syntaxContext.setPredicateNode(predicateNodes.get(root));
        }
        if (lex.containsKey("order by")) {
            parseOrderBy(Helper.normalize(lex.get("order by")));
            syntaxContext.setOrderNodes(parseOrderBy(lex.get("order by")));
        }
        syntaxContext.setDistinct(distinct);
        checkSyntaxContext(expression, syntaxContext, lex);
        return syntaxContext;
    }

    private List<ColumnNode> parseOrderBy(String in) {
        List<ColumnNode> result = new ArrayList<>();
        for (String item : in.split("[,]")) {
            ColumnNode columnNode = parseColumnNode(item, "order by");
            result.add(columnNode);
        }
        return result;
    }

    private List<ColumnNode> parseSelectNode(String in) {
        List<ColumnNode> columnNodes = new ArrayList<>();
        String regex = "(?<=^|,).*?(?=$|,)";
        Pattern pt = Pattern.compile(regex);
        Matcher mt = pt.matcher(in);
        while (mt.find()) {
            columnNodes.add(parseColumnNode(in.substring(mt.start(), mt.end()), "select"));
        }
        return columnNodes;
    }

    private ColumnNode parseColumnNode(String in, String lex) {
        String regex = "^[\\s]*([a-zA-Z_0-9]+){0,1}([.]){0,1}([a-zA-Z_0-9]+){0,1}[\\s]*(\\[\\+\\]){0,1}[\\s]*(asc|desc){0,1}[\\s]*$";
        Pattern pt = Pattern.compile(regex);
        Matcher mt = pt.matcher(in);
        if (mt.find()) {
            String part1 = mt.group(1);
            String part2 = mt.group(3);
            String part3 = mt.group(4);
            String part4 = mt.group(5);
            if ("select".equals(lex) && (!StringUtils.isBlank(part3) || !StringUtils.isBlank(part4))) {
                //TODO CollectionException
                throw new CollectionException("Not use " + part3 + " and " + part4 + " in select", in);
            }
            if ("order by".equals(lex) && !StringUtils.isBlank(part3)) {
                //TODO CollectionException
                throw new CollectionException("Not use " + part3 + " in order by", in);
            }
            if ("where".equals(lex) && !StringUtils.isBlank(part4)) {
                //TODO CollectionException
                throw new CollectionException("Not use " + part4 + " in where", in);
            }
            String columnName = StringUtils.defaultString(part2, part1);
            String tableName = StringUtils.isBlank(part2) ? StringUtils.EMPTY : part1;
            ColumnNode columnNode = SyntaxContext.createColumnNode(tableName, columnName);
            columnNode.setOuterJoin(!StringUtils.isBlank(part3));
            if ("order by".equals(lex)) {
                columnNode.setAsc(StringUtils.isBlank(part4) ? true : "asc".equals(part4));
            }
            return columnNode;
        } else {
            //TODO CollectionException
            throw new CollectionException("Expected table1.c1 or c1", in);
        }
    }

    private List<TableNode> parseFromNode(String in) {
        List<TableNode> tableNodes = new ArrayList<>();
        String regex = "(?<=^|,).*?(?=$|,)";
        Pattern pt = Pattern.compile(regex);
        Matcher mt = pt.matcher(in);
        while (mt.find()) {
            String fullTableName = in.substring(mt.start(), mt.end());
            String regexTable = "^[\\s]*([a-zA-Z_0-9]+)[\\s]*([a-zA-Z_0-9]*)[\\s]*$";
            Pattern pt2 = Pattern.compile(regexTable);
            Matcher mt2 = pt2.matcher(fullTableName);
            if (!mt2.find()) {
                //TODO CollectionException
                throw new CollectionException("Not found table name: " + fullTableName, in);
            }
            String tableName = mt2.group(1);
            String shortName = StringUtils.defaultString(mt2.group(2), StringUtils.EMPTY);
            tableNodes.add(SyntaxContext.createTableNode(tableName, shortName));
        }
        return tableNodes;
    }

    private String parsePredicateNode(String in) {
        if (!in.matches("[()@\\w\\s.?<=>+\\[\\]]*")) {
            //TODO CollectionException
            throw new CollectionException(in);
        }
        String nodeName = null;
        String expression = in;
        Pattern pattern = Pattern.compile("\\s*\\(([\\w\\s.\\[\\]+<=>?@&&[^()]]+)\\)\\s*");
        while (true) {
            Matcher matcher = pattern.matcher(expression);
            if (matcher.find()) {
                nodeName = parseExpressionOfPredicate(matcher.group(1), Predicate.AND.KEY);
                predicateNodes.get(nodeName).incBracketCount();
                StringBuffer sb = new StringBuffer();
                matcher.appendReplacement(sb, " " + nodeName + " ");
                matcher.appendTail(sb);
                expression = sb.toString();
            } else {
                break;
            }
        }
        if (!expression.matches("\\s*@\\w\\s*")) {
            return parseExpressionOfPredicate(expression, Predicate.AND.KEY);
        } else {
            if (StringUtils.isBlank(nodeName)) {
                //TODO CollectionException
                throw new CollectionException("The name is empty.");
            }
            return nodeName;
        }
    }

    private String parseExpressionOfPredicate(String in, String action) {
        String normal = in.trim();
        String regex = "^[\\s]*(.+)[\\s]*(" + action + ")[\\s]*(.+)[\\s]*$";
        Pattern pt = Pattern.compile(regex);
        Matcher mt = pt.matcher(normal);
        if (mt.find()) {
            String name = createName();
            String left = mt.group(1).trim();
            String right = mt.group(3).trim();
            left = parseExpressionOfPredicate(left, action);
            right = parseExpressionOfPredicate(right, action);
            PredicateNode node = SyntaxContext.createPredicateNode();
            node.setName(name);
            node.setPredicate(Predicate.getByKey(action));
            node.setExpression(left + " " + action + " " + right);
            node.add(predicateNodes.containsKey(left) ? predicateNodes.get(left) : parseNoActionNode(left));
            node.add(predicateNodes.containsKey(right) ? predicateNodes.get(right) : parseNoActionNode(right));
            predicateNodes.put(name, node);
            return name;
        } else {
            String nextAction = actionOrder.get(action);
            return nextAction == null ? normal : parseExpressionOfPredicate(in, nextAction);
        }
    }

    private Map<String, String> parseSqlLex(String in) {
        Pattern pattern = Pattern.compile("(?<=select ).*?(?= from )");
        Matcher matcher = pattern.matcher(in);
        Map<String, String> lexs = new HashMap<>();
        if (matcher.find()) {
            String s = in.substring(matcher.start(), matcher.end()).trim();
            pattern = Pattern.compile("(?<=distinct ).*?(?=$)");
            matcher = pattern.matcher(s);
            if (matcher.find()) {
                distinct = true;
                s = s.substring(matcher.start(), matcher.end()).trim();
            }
            lexs.put("select", s);
        }
        pattern = Pattern.compile("(?<= from ).*?(?= where | order by |$)");
        matcher = pattern.matcher(in);
        if (matcher.find()) {
            lexs.put("from", in.substring(matcher.start(), matcher.end()).trim());
        }
        pattern = Pattern.compile("(?<= where ).*?(?= order by |$)");
        matcher = pattern.matcher(in);
        if (matcher.find()) {
            lexs.put("where", in.substring(matcher.start(), matcher.end()).trim());
        }
        pattern = Pattern.compile("(?<= order by ).*?(?=$)");
        matcher = pattern.matcher(in);
        if (matcher.find()) {
            lexs.put("order by", in.substring(matcher.start(), matcher.end()).trim());
        }
        return lexs;
    }

    private SyntaxNode parseNoActionNode(String expression) {
        if (expression.startsWith("?")) {
            return SyntaxContext.createParamNode(expression.substring(1));
        } else {
            return parseColumnNode(expression, "where");
        }
    }

    private String createName() {
        return "@" + count++;
    }

    private void checkSyntaxContext(String sqlText, SyntaxContext context, Map<String, String> lex) {
        if (context.getSelectNodes().isEmpty()) {
            //TODO CollectionException
            throw new CollectionException("Select nodes are empty.");
        }
        if (context.getTableNodes().isEmpty()) {
            //TODO CollectionException
            throw new CollectionException("Table nodes are empty.");
        }

        String where = "";
        if (lex.containsKey("where")) {
            CheckSyntaxVisitor visitor = new CheckSyntaxVisitor();
            context.getPredicateNode().visit(visitor);
            where = "[\\s]+where[\\s]+" + visitor.expressions.pop();
        }

        String select = "select[\\s]+" + (context.isDistinct() ? "distinct[\\s]+" : "");
        boolean start = true;
        for (ColumnNode cn : context.getSelectNodes()) {
            if (!start) {
                select += "[\\s]*,[\\s]*";
            }
            select += cn.check() + "[\\s]*";
            start = false;
        }

        String from = "[\\s]+from[\\s]+";
        start = true;
        for (TableNode tn : context.getTableNodes()) {
            if (!start) {
                from += "[\\s]*,[\\s]*";
            }
            from += "[\\s]*" + tn.check() + "[\\s]*";
            start = false;
        }

        String order = "";
        if (lex.containsKey("order by")) {
            start = true;
            order = "[\\s]+order by[\\s]+";
            for (ColumnNode cn : context.getOrderNodes()) {
                if (!start) {
                    order += "[\\s]*,[\\s]*";
                }
                order += cn.check();
                start = false;
            }
        }

        String actSqlText = select + from + where + order;
        if (!sqlText.matches(actSqlText)) {
            //TODO CollectionException
            throw new CollectionException("expect: " + sqlText, " actual: " + actSqlText);
        }
    }

    private class CheckSyntaxVisitor implements SyntaxVisitor {
        private Stack<String> expressions = new Stack<>();

        public void perform(SyntaxNode syntaxNode) {
            if (syntaxNode instanceof ColumnNode) {
                expressions.push(syntaxNode.check());
            } else if (syntaxNode instanceof ParameterNode) {
                expressions.push(syntaxNode.check());
            } else if (syntaxNode instanceof PredicateNode) {
                syntaxNode.check();
                PredicateNode predicateNode = (PredicateNode) syntaxNode;
                StringBuilder sb = new StringBuilder();
                String right = expressions.pop();
                String left = expressions.pop();
                sb.append("[\\s]*");
                if (predicateNode.isBracket()) {
                    int i = predicateNode.getBracketCount();
                    while (i-- > 0) {
                        sb.append("[(]{1}");
                        sb.append("[\\s]*");
                    }
                }
                sb.append(left);
                sb.append("[\\s]*");
                sb.append(predicateNode.getPredicate().KEY);
                sb.append("[\\s]*");
                sb.append(right);
                sb.append("[\\s]*");
                if (predicateNode.isBracket()) {
                    int i = predicateNode.getBracketCount();
                    while (i-- > 0) {
                        sb.append("[)]{1}");
                        sb.append("[\\s]*");
                    }
                }
                expressions.push(sb.toString());
            }
        }
    }
}
