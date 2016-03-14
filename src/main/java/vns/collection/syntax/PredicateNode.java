package vns.collection.syntax;

import org.apache.commons.lang3.StringUtils;
import vns.collection.Predicate;

/**
 * PredicateNode
 */
public class PredicateNode extends SyntaxNode {
    private String name;
    private String expression;
    private int bracketCount;
    private Predicate predicate;

    PredicateNode() {
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate predicate) {
        this.predicate = predicate;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBracket() {
        return bracketCount > 0;
    }

    public int getBracketCount() {
        return bracketCount;
    }

    public void incBracketCount() {
        this.bracketCount++;
    }

    @Override
    public String check() {
        if (Predicate.AND == getPredicate() || Predicate.OR == getPredicate()) {
            for (SyntaxNode node : getChilds()) {
                if (!(node instanceof PredicateNode)) {
                    //TODO ClassCastException;
                    throw new ClassCastException("The node isn't predicate");
                }
            }
        } else {
            for (SyntaxNode node : getChilds()) {
                if (node instanceof PredicateNode) {
                    //TODO ClassCastException;
                    throw new ClassCastException("The node isn't predicate");
                }
            }
        }
        return StringUtils.EMPTY;
    }
}
