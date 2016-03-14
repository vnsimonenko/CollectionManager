package vns.collection.syntax;

import java.util.ArrayList;
import java.util.List;

/**
 * SyntaxNode
 */
public abstract class SyntaxNode {
    private List<SyntaxNode> childs = new ArrayList<>();

    /**
     * It form regex from from initialization fields
     * and check syntax inner fields.
     *
     * @return String regexp.
     */
    public abstract String check();

    public void add(SyntaxNode node) {
        childs.add(node);
    }

    public List<SyntaxNode> getChilds() {
        return childs;
    }

    /**
     * for form syntax tree and check syntax tree
     *
     * @param visitor
     */
    public void visit(SyntaxVisitor visitor) {
        for (SyntaxNode child : childs) {
            child.visit(visitor);
        }
        visitor.perform(this);
    }
}