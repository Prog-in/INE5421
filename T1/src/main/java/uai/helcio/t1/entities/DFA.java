package uai.helcio.t1.entities;

import uai.helcio.t1.entities.RegexNode.BinaryNode;
import uai.helcio.t1.entities.RegexNode.UnaryNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DFA {
    private final RegexTree tree;

    public DFA(RegexTree tree) {
        this.tree = tree;
    }

    public String getTokenName() {
        return tree.getTreeName();
    }

    public boolean accepts(String input) {
        Set<Integer> endPositions = matchNode(tree.getRoot(), input, 0);
        return endPositions.contains(input.length());
    }

    private Set<Integer> matchNode(RegexNode node, String input, int currentPosition) {
        // Base case
        if (RegexNode.END_NODE_SYMBOL.equals(node.getVal())) {
            return new HashSet<>(Collections.singletonList(currentPosition)); // singletonList doesn't allocate more memory
        }

        // Leaf node
        if (!(node instanceof BinaryNode) && !(node instanceof UnaryNode)) {
            if (currentPosition < input.length()) {
                String currChar = String.valueOf(input.charAt(currentPosition));
                if (node.getVal().equals(currChar)) {
                    return new HashSet<>(Collections.singletonList(currentPosition + 1));
                }
            }
            return Collections.emptySet();
        }

        // Binary Node
        if (node instanceof BinaryNode binaryNode) {
            String operator = binaryNode.getVal();
            if ("|".equals(operator)) {
               // Match left or match right
                Set<Integer> results = new HashSet<>();
                results.addAll(matchNode(binaryNode.left, input, currentPosition));
                results.addAll(matchNode(binaryNode.right, input, currentPosition));
                return results;
            } else if (".".equals(operator)) {
                // Match left then match right from where the left side ended
                Set<Integer> leftEnds = matchNode(binaryNode.left, input, currentPosition);
                Set<Integer> results = new HashSet<>();
                for (int end : leftEnds) {
                    results.addAll(matchNode(binaryNode.right, input, end));
                }
                return results;
            }
        }

        // Unary node
        if (node instanceof UnaryNode unaryNode) {
            String operator = unaryNode.getVal();

            // ? := match 0 or 1 time
            if ("?".equals(operator)) {
                Set<Integer> results = new HashSet<>();
                results.add(currentPosition); // Match 0
                results.addAll(matchNode(unaryNode.kid, input, currentPosition)); // Match 1
                return results;
            }

            // * := match 0 or N times
            if ("*".equals(operator)) {
                Set<Integer> results = new HashSet<>();
                results.add(currentPosition); // Match 0

                // Match N
                collectRepetitions(unaryNode.kid, input, results, currentPosition);
                return results;
            }

            // + :- match 1 or N times
            if ("+".equals(operator)) {
                Set<Integer> firstMatches = matchNode(unaryNode.kid, input, currentPosition);
                if (firstMatches.isEmpty()) {
                    return Collections.emptySet(); // Must match at least once
                }

                Set<Integer> results = new HashSet<>(firstMatches);

                // Try matching again from all former matches
                for (int matchEnd : firstMatches) {
                    collectRepetitions(unaryNode.kid, input, results, matchEnd);
                }
                return results;
            }
        }

        return Collections.emptySet();
    }


    private void collectRepetitions(RegexNode kid, String input, Set<Integer> results, int startPos) {
        Set<Integer> currentEnds = new HashSet<>();
        currentEnds.add(startPos);

        boolean changed = true;
        while (changed) {
            changed = false;
            Set<Integer> nextEnds = new HashSet<>();
            for (int pos : currentEnds) {
                Set<Integer> matches = matchNode(kid, input, pos);
                for (int m : matches) {
                    // Only proceeds if we consumed m > pos characters in order to avoid deadlock and state repetition
                    if (m > pos && !results.contains(m)) {
                        results.add(m);
                        nextEnds.add(m);
                        changed = true;
                    }
                }
            }
            currentEnds = nextEnds;
        }
    }
}