package uai.helcio.t1.entities;

import uai.helcio.t1.entities.RegexNode.BinaryNode;
import uai.helcio.t1.entities.RegexNode.LeafNode;
import uai.helcio.t1.entities.RegexNode.UnaryNode;
import uai.helcio.utils.AppLogger;

import java.util.*;

public class RegexTree {

    private final String treeName;
    private final BinaryNode root;

    // position -> following positions
    private final Map<Integer, Set<Integer>> followpos = new HashMap<>();

    // position-> symbol
    private final Map<Integer, String> inputs = new HashMap<>();

    private int leafCount = 0;

    public RegexTree(String name, RegexNode rawLeftSubTree) {
        this.treeName = name;

        this.root = BinaryNode.createAugmentedRoot(rawLeftSubTree);

        initialize();
    }

    private void initialize() {
        AppLogger.logger.debug("=== Initializing Tree: {} ===", treeName);
        assignLeavesIndices(root); // enumerates leaves
        root.calculateFunctions(); // evaluate nullable, firstpos and lastpos
        AppLogger.logger.trace("Root firstpos: {}", root.getFirstpos());

        for (int i = 1; i <= leafCount; i++) {
            followpos.put(i, new HashSet<>());
        }

        // evaluate lastpos
        computeFollowpos(root);
        AppLogger.logger.trace("Followpos table: {}", followpos);
    }


    /**
     * Runs the tree adding leaf index
     * @param node curr node
     */
    private void assignLeavesIndices(RegexNode node) {
        switch (node) {
            case null -> {}
            case LeafNode leaf -> {
                // epsilon get's no value
                if (!RegexNode.EPSILON.equals(leaf.getVal())) {
                    leafCount++;
                    leaf.setPosition(leafCount);
                    inputs.put(leafCount, leaf.getVal());
                }
            }
            case BinaryNode bin -> {
                assignLeavesIndices(bin.left);
                assignLeavesIndices(bin.right);
            }
            case UnaryNode un -> assignLeavesIndices(un.kid);
            default -> {
            }
        }
    }

    private void computeFollowpos(RegexNode node) {
        switch (node) {
            case null -> {}
            case BinaryNode bin -> {
                if (".".equals(bin.getVal())) {
                    // concat := N = c1.c2 -> for each i in lastpos(c1) add firstpos(c2) to followpos(i)
                    for (int i : bin.left.getLastpos()) {
                        followpos.get(i).addAll(bin.right.getFirstpos());
                    }
                }
                computeFollowpos(bin.left);
                computeFollowpos(bin.right);
            }
            case UnaryNode un -> {
                if ("*".equals(un.getVal()) || "+".equals(un.getVal())) {
                    // repetition := N = c1* -> for each i em lastpos(c1) add firstpos(c1) ao followpos(i)
                    for (int i : un.getLastpos()) {
                        followpos.get(i).addAll(un.getFirstpos());
                    }
                }
                computeFollowpos(un.kid);
            }
            default -> {
            }
        }

    }

    public BinaryNode getRoot() {
        return root;
    }

    public Map<Integer, Set<Integer>> getFollowpos() {
        return followpos;
    }

    public String getSymbol(int position) {
        return inputs.get(position);
    }
    public Set<String> getAlphabet() {
        Set<String> alphabet = new HashSet<>(inputs.values());
        alphabet.remove(RegexNode.END_NODE_SYMBOL);
        return alphabet;
    }

    public String getTreeName() {
        return treeName;
    }

    @Override
    public String toString() {
        return String.format("RegexTree[%s] - Leaves: %d", treeName, leafCount);
    }
}