package uai.helcio.t1.Automata;

import uai.helcio.t1.utils.AnsiColors;
import uai.helcio.utils.AppLogger;

import java.util.*;

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

        List<Integer> sortedStates = new ArrayList<>(transitionTable.keySet());
        Collections.sort(sortedStates);

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
            Map<Integer, List<String>> grouped = new TreeMap<>();

            for (Map.Entry<String, Integer> entry : trans.entrySet()) {
                grouped.computeIfAbsent(entry.getValue(), _ -> new ArrayList<>()).add(entry.getKey());
            }

            for (Map.Entry<Integer, List<String>> group : grouped.entrySet()) {
                String dest = String.valueOf(group.getKey());
                List<String> symbolsList = group.getValue();

                int maxSymLen = 35;
                StringBuilder buffer = new StringBuilder();
                buffer.append("[");

                symbolsList.forEach(sym -> {
                    String separator = (buffer.length() > 1) ? ", " : "";
                    if (buffer.length() + separator.length() + sym.length() + 1 > maxSymLen && buffer.length() > 1) {
                        buffer.append("]");
                        sb.append(String.format("   --> %-5s via %s\n", dest, buffer));

                        buffer.setLength(0);
                        buffer.append("[").append(sym);
                    } else {
                        buffer.append(separator).append(sym);
                    }
                });

                if (!buffer.isEmpty()) {
                    buffer.append("]");
                    sb.append(String.format("   --> %-5s via %s\n", dest, buffer));
                }

                if (i < sortedStates.size() - 1) {
                    sb.append(thinLine).append("\n");
                }
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