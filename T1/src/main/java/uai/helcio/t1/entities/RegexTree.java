package uai.helcio.t1.entities;

import java.util.ArrayList;
import java.util.List;
import uai.helcio.t1.entities.RegexNode.*;

public class RegexTree {

    private final String treeName;
    private final BinaryNode root;

    @Override
    public String toString() {
        StringBuilder treeString = new StringBuilder();
        for (RegexNode node : traverseInOrder()) {
            treeString.append(String.format("%s ", node.toString()));
        }
        return String.format("[ treeName=%s, nodes=[ %s ] ]", treeName, treeString);
    }

    public RegexTree(String name, RegexNode leftSubTree) {
        treeName = name;
        this.root = BinaryNode.getRoot(leftSubTree);
    }

    public String getTreeName() {
        return treeName;
    }

    public BinaryNode getRoot() {
        return root;
    }

    private List<RegexNode> traverseInOrder() {
        List<RegexNode> nodes = new ArrayList<>();
        traverseInOrder_(root, nodes);
        return nodes;
    }

    private void traverseInOrder_(RegexNode parent, List<RegexNode> preorderNodes) {
        switch (parent) {
            case BinaryNode binaryParent -> {
                if (binaryParent.left != null) {
                    traverseInOrder_(binaryParent.left, preorderNodes);
                }
                preorderNodes.add(parent);
                if (binaryParent.right != null) {
                    traverseInOrder_(binaryParent.right, preorderNodes);
                }
            }
            case UnaryNode unaryParent -> {
                if (unaryParent.kid != null) {
                    traverseInOrder_(unaryParent.kid, preorderNodes);
                }
                preorderNodes.add(parent);
            }
            case null -> {}
            default -> preorderNodes.add(parent);
        }
    }
}
