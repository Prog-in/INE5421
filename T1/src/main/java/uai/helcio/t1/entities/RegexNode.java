package uai.helcio.t1.entities;

import java.util.HashSet;
import java.util.Set;

public abstract class RegexNode {
    public static final String END_NODE_SYMBOL = "#";
    public static final String EPSILON = "ε";
    protected boolean nullable;
    protected Set<Integer> firstpos = new HashSet<>();
    protected Set<Integer> lastpos = new HashSet<>();
    protected int position = 0;

    protected RegexNode parent;
    protected final String val;

    public RegexNode(String val, RegexNode parent) {
        this.val = val;
        this.parent = parent;
    }

    public abstract void calculateFunctions();

    public boolean isNullable() {
        return nullable;
    }

    public Set<Integer> getFirstpos() {
        return firstpos;
    }

    public Set<Integer> getLastpos() {
        return lastpos;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getVal() {
        return val;
    }

    @Override
    public String toString() {
        return val;
    }

    public void setParent(RegexNode parent) {
        this.parent = parent;
    }

    public static class LeafNode extends RegexNode {
        public LeafNode(RegexNode parent, String val) {
            super(val, parent);
        }

        @Override
        public void calculateFunctions() {
            if (EPSILON.equals(val)) {
                this.nullable = true;
                this.firstpos.clear();
                this.lastpos.clear();
            }
            else {
                this.nullable = false;
                this.firstpos.add(this.position);
                this.lastpos.add(this.position);
            }
        }
    }


    public static class BinaryNode extends RegexNode {
        public RegexNode left;
        public RegexNode right;

        public BinaryNode(RegexNode parent, String val) {
            super(val, parent);
        }

        public static BinaryNode createAugmentedRoot(RegexNode leftSubTree) {
            BinaryNode root = new BinaryNode(null, ".");
            root.left = leftSubTree;
            root.right = new LeafNode(root, END_NODE_SYMBOL);

            if (leftSubTree != null) leftSubTree.parent = root;

            return root;
        }

        @Override
        public void calculateFunctions() {
            if (left != null) left.calculateFunctions();
            if (right != null) right.calculateFunctions();

            if ("|".equals(val)) {
                this.nullable = left.isNullable() || right.isNullable();

                // firstpos = firstpos(c1) U firstpos(c2)
                this.firstpos.addAll(left.getFirstpos());
                this.firstpos.addAll(right.getFirstpos());

                // lastpos = lastpos(c1) U lastpos(c2)
                this.lastpos.addAll(left.getLastpos());
                this.lastpos.addAll(right.getLastpos());
            }
            else if (".".equals(val)) { // CAT Node
                // nullable = nullable(c1) AND nullable(c2)
                this.nullable = left.isNullable() && right.isNullable();

                // if (nullable(c1)) firstpos(c1) U firstpos(c2) else firstpos(c1)
                this.firstpos.addAll(left.getFirstpos());
                if (left.isNullable()) {
                    this.firstpos.addAll(right.getFirstpos());
                }

                // if (nullable(c2)) lastpos(c1) U lastpos(c2) else lastpos(c2)
                this.lastpos.addAll(right.getLastpos());
                if (right.isNullable()) {
                    this.lastpos.addAll(left.getLastpos());
                }
            }
        }
    }

    public static class UnaryNode extends RegexNode {
        public RegexNode kid;

        public UnaryNode(RegexNode parent, String val) {
            super(val, parent);
        }

        @Override
        public void calculateFunctions() {
            if (kid != null) kid.calculateFunctions();

            if ("*".equals(val)) {
                this.nullable = true;
                this.firstpos.addAll(kid.getFirstpos());
                this.lastpos.addAll(kid.getLastpos());
            }
            else if ("+".equals(val)) { // a+ == a.a*
                this.nullable = kid.isNullable();
                this.firstpos.addAll(kid.getFirstpos());
                this.lastpos.addAll(kid.getLastpos());
            }
            else if ("?".equals(val)) { // a? == (a|epsilon)
                this.nullable = true;
                this.firstpos.addAll(kid.getFirstpos());
                this.lastpos.addAll(kid.getLastpos());
            }
        }
    }
}