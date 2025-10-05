package uai.helcio.entities;

public class RegexNode {
    public static final String END_NODE_SYMBOL = "#";

    public RegexNode parent;
    protected final String val;

    @Override
    public String toString() {
        return val;
    }

    public RegexNode(String val, RegexNode parent) {
        this.val = val;
        this.parent = parent;
    }

    public RegexNode getParent() {
        return parent;
    }

    public String getVal() {
        return val;
    }

    public static class BinaryNode extends RegexNode {
        public RegexNode left;
        public RegexNode right;

        public static BinaryNode getRoot(RegexNode leftSubTree) {
            BinaryNode root = new BinaryNode(null, ".");
            root.left = leftSubTree;
            root.right = new BinaryNode(root, END_NODE_SYMBOL);
            return root;
        }

        public BinaryNode(RegexNode parent, String val) {
            super(val, parent);
            this.left = null;
            this.right = null;
        }
    }

    public static class UnaryNode extends RegexNode {
        public RegexNode kid;

        public UnaryNode(RegexNode parent, String val) {
            super(val, parent);
            this.kid = null;
        }
    }
}
