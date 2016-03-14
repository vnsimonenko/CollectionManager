package vns.collection.syntax;

/**
 * ParameterNode
 */
public class ParameterNode extends SyntaxNode {
    private Integer index;

    public ParameterNode(String index) {
        this.index = Integer.valueOf(index);
    }

    public Integer getIndex() {
        return index;
    }

    @Override
    public String check() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\\s]*\\?");
        sb.append(index);
        sb.append("[\\s]*");
        return sb.toString();
    }
}
