package uai.helcio.t1.Automata;

import uai.helcio.t1.utils.AnsiColors;
import uai.helcio.utils.AppLogger;

import java.util.*;

public class NFA {
    public static final int START_STATE = 0;

    // state -> (symbol -> next states set)
    private final Map<Integer, Map<String, Set<Integer>>> transitions = new HashMap<>();

    // state -> next states set
    private final Map<Integer, Set<Integer>> epsilonTransitions = new HashMap<>();

    // id -> token
    private final Map<Integer, String> finalStateTokens = new HashMap<>();

    // alphabet required for latter determinization
    private final Set<String> alphabet = new HashSet<>();

    public void addTransition(int from, String symbol, int to) {
        transitions.computeIfAbsent(from, k -> new HashMap<>())
                .computeIfAbsent(symbol, k -> new HashSet<>())
                .add(to);
        alphabet.add(symbol);
    }

    public void addEpsilonTransition(int from, int to) {
        epsilonTransitions.computeIfAbsent(from, k -> new HashSet<>()).add(to);
    }

    public void addFinalState(int state, String tokenName) {
        finalStateTokens.putIfAbsent(state, tokenName);
    }

    public Map<String, Set<Integer>> getTransitions(int state) {
        return transitions.getOrDefault(state, Collections.emptyMap());
    }

    public Set<Integer> getEpsilonTransitions(int state) {
        return epsilonTransitions.getOrDefault(state, Collections.emptySet());
    }

    public Map<Integer, String> getFinalStateTokens() {
        return finalStateTokens;
    }

    public Set<String> getAlphabet() {
        return alphabet;
    }

    @Override
    public String toString() {
        return String.format("NFA [States: %d, Transitions: %d, Epsilons: %d]",
                transitions.size(),
                transitions.values().stream().mapToInt(m -> m.size()).sum(),
                epsilonTransitions.size());
    }

    public void logStructure(String stageName) {
        final int LINE_LEN = 60;
        String thickLine = "═".repeat(LINE_LEN);
        String thinLine = "─".repeat(LINE_LEN);

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(String.format("NFA STRUCTURE: %s\n", stageName));
        sb.append(thickLine).append("\n");

        Set<Integer> allStates = new HashSet<>();
        allStates.addAll(transitions.keySet());
        allStates.addAll(epsilonTransitions.keySet());
        allStates.addAll(finalStateTokens.keySet());
        allStates.add(START_STATE);

        java.util.List<Integer> sortedStates = new java.util.ArrayList<>(allStates);
        java.util.Collections.sort(sortedStates);

        for (int i = 0; i < sortedStates.size(); i++) {
            int state = sortedStates.get(i);
            String type = (state == START_STATE) ? "START" : "";
            if (finalStateTokens.containsKey(state)) {
                type += (type.isEmpty() ? "" : ", ") + "FINAL(" + finalStateTokens.get(state) + ")";
            }

            String stateInfo = "State " + state + (type.isEmpty() ? "" : " [" + type + "]");
            sb.append(AnsiColors.CYAN_BOLD)
                    .append(stateInfo)
                    .append(AnsiColors.RESET)
                    .append("\n");

            if (epsilonTransitions.containsKey(state)) {
                String targets = epsilonTransitions.get(state).toString();
                sb.append(String.format("   --> %-15s (EPSILON)\n", targets));
            }

            if (transitions.containsKey(state)) {
                Map<String, Set<Integer>> trans = transitions.get(state);


                Map<String, java.util.List<String>> grouped = new java.util.TreeMap<>();

                for (Map.Entry<String, Set<Integer>> entry : trans.entrySet()) {
                    String targetStr = entry.getValue().toString();
                    grouped.computeIfAbsent(targetStr, k -> new java.util.ArrayList<>()).add(entry.getKey());
                }

                for (Map.Entry<String, java.util.List<String>> group : grouped.entrySet()) {
                    String targets = group.getKey(); // Ex: "[2]"
                    String symbols = group.getValue().toString(); // Ex: "[A, B, C...]"

                    // truncate for qol
                    int maxSymLen = 33;
                    if (symbols.length() > maxSymLen) {
                        symbols = symbols.substring(0, maxSymLen - 3) + "...";
                    }

                    sb.append(String.format("   --> %-15s via %s\n", targets, symbols));
                }
            }

            if (i < sortedStates.size() - 1) {
                sb.append(thinLine).append("\n");
            }
        }

        sb.append(thickLine).append("\n");
        AppLogger.logger.info(sb.toString());
    }
}