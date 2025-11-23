package uai.helcio.t1.Automata;

import uai.helcio.t1.utils.AnsiColors;
import uai.helcio.utils.AppLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DFA {
    private final String name;
    private final int startState = 0;
    private final Set<Integer> finalStates;
    private final Map<Integer, Map<String, Integer>> transitionTable;

    // Map<ID, TokenName>
    private final Map<Integer, String> finalStateTags;

    // Constructor for a single regex dfa
    public DFA(String name, Set<Integer> finalStates, Map<Integer, Map<String, Integer>> transitionTable) {
        this(name, finalStates, transitionTable, new HashMap<>());
    }

    // Constructor for multiple regex dfa
    public DFA(String name, Set<Integer> finalStates, Map<Integer, Map<String, Integer>> transitionTable, Map<Integer, String> finalStateTags) {
        this.name = name;
        this.finalStates = finalStates;
        this.transitionTable = transitionTable;
        this.finalStateTags = finalStateTags;
    }

    public String getTokenName() {
        return name;
    }

    public Map<Integer, Map<String, Integer>> getTransitionTable() {
        return transitionTable;
    }

    public Set<Integer> getFinalStates() {
        return finalStates;
    }


    public void logStructure(String stageName) {
        final int LINE_LEN = 60;
        String thickLine = "═".repeat(LINE_LEN);
        String thinLine = "─".repeat(LINE_LEN);

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(String.format("DFA STRUCTURE: %s (%s)\n", stageName, name));
        sb.append(thickLine).append("\n");

        java.util.List<Integer> sortedStates = new java.util.ArrayList<>(transitionTable.keySet());
        java.util.Collections.sort(sortedStates);

        for (int i = 0; i < sortedStates.size(); i++) {
            int state = sortedStates.get(i);
            String type = (state == startState) ? "START" : "";
            if (finalStates.contains(state)) {
                String token = finalStateTags.getOrDefault(state, name);
                type += (type.isEmpty() ? "" : ", ") + "FINAL(" + token + ")";
            }

            String stateInfo = "State " + state + (type.isEmpty() ? "" : " [" + type + "]");

            sb.append(AnsiColors.CYAN_BOLD).append(stateInfo).append(AnsiColors.RESET).append("\n");

            Map<String, Integer> trans = transitionTable.get(state);
            Map<Integer, java.util.List<String>> grouped = new java.util.TreeMap<>();

            for (Map.Entry<String, Integer> entry : trans.entrySet()) {
                grouped.computeIfAbsent(entry.getValue(), k -> new java.util.ArrayList<>()).add(entry.getKey());
            }

            for (Map.Entry<Integer, java.util.List<String>> group : grouped.entrySet()) {
                String dest = String.valueOf(group.getKey());
                String symbols = group.getValue().toString();

                // truncating for qol
                int maxSymLen = LINE_LEN - 20;
                if (symbols.length() > maxSymLen) {
                    symbols = symbols.substring(0, maxSymLen - 3) + "...";
                }

                sb.append(String.format("   --> %s via %s\n", dest, symbols));
            }

            if (i < sortedStates.size() - 1) {
                sb.append(thinLine).append("\n");
            }
        }
        sb.append(thickLine).append("\n");

        AppLogger.logger.info(sb.toString());
    }

    public record TokenResult(String tokenName, String lexeme, int endPosition) {}

    /**
     * Try finding the longest token from startPos
     * @param input the string to be analyzed
     * @param startPos the startPos
     * @return the next token
     */
    public TokenResult nextToken(String input, int startPos) {
        int currentState = startState;
        int lastFinalState = -1;
        int lastFinalPos = -1;

        int currentPos = startPos;

        // while there's valid transitions
        while (currentPos < input.length()) {
            String symbol = String.valueOf(input.charAt(currentPos));
            Map<String, Integer> transitions = transitionTable.get(currentState);

            // If there's no transition
            if (transitions == null || !transitions.containsKey(symbol)) {
                break;
            }

            currentState = transitions.get(symbol);
            currentPos++;

            // checkPoint as we want the longest string possible (in order to re-evaluate)
            if (finalStates.contains(currentState)) {
                lastFinalState = currentState;
                lastFinalPos = currentPos;
            }
        }

        // If it's a valid finalState
        if (lastFinalState != -1) {
            // we return the largest token
            String tokenName = finalStateTags.getOrDefault(lastFinalState, name);
            String lexeme = input.substring(startPos, lastFinalPos);
            return new TokenResult(tokenName, lexeme, lastFinalPos);
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format("DFA[%s] States: %d, Tags: %s", name, transitionTable.size(), finalStateTags);
    }

    public Map<Integer, String> getFinalStateTags() {
        return finalStateTags;
    }
}