package uai.helcio.t1.Automata;

import uai.helcio.utils.AppLogger;

import java.util.*;

public class DFAMinimizer {

    public static DFA minimize(DFA dfa) {
        AppLogger.logger.debug(">> Iniciando Minimização para: {}", dfa.getTokenName());
        Map<Integer, Map<String, Integer>> trans = dfa.getTransitionTable();
        Set<Integer> finalStates = dfa.getFinalStates();

        // used for minimizing the lexical analyzer
        Map<Integer, String> tags = dfa.getFinalStateTags();

        Set<Integer> allStates = trans.keySet();
        Set<String> alphabet = new HashSet<>();
        for (Map<String, Integer> map : trans.values()) {
            alphabet.addAll(map.keySet());
        }

        Set<Integer> nonFinalStates = new HashSet<>(allStates);
        nonFinalStates.removeAll(finalStates);

        List<Set<Integer>> partitions = new ArrayList<>();

        // If tags exist final states must be split by Token Name.
        // If not, all final states are equivalent.
        if (!tags.isEmpty()) {
            Map<String, Set<Integer>> finalsByToken = new HashMap<>();
            for (int s : finalStates) {
                String token = tags.get(s);
                finalsByToken.computeIfAbsent(token, _ -> new HashSet<>()).add(s);
            }
            partitions.addAll(finalsByToken.values());
        } else {
            if (!finalStates.isEmpty()) partitions.add(finalStates);
        }

        if (!nonFinalStates.isEmpty()) partitions.add(nonFinalStates);

        // Hopcroft
        boolean changed = true;
        int pass = 1;
        while (changed) {
            changed = false;
            AppLogger.logger.trace("--- Relaxing step #{} ---", pass++);
            List<Set<Integer>> newPartitions = new ArrayList<>();

            for (Set<Integer> group : partitions) {
                if (group.size() <= 1) {
                    newPartitions.add(group);
                    continue;
                }

                // try splitting the current group
                Map<String, Set<Integer>> splitter = new HashMap<>();

                for (int state : group) {
                    // unique key based on what group each transition goes
                    StringBuilder signature = new StringBuilder();
                    for (String symbol : alphabet) {
                        int target = trans.get(state).getOrDefault(symbol, -1);
                        int targetGroupIndex = findGroupIndex(target, partitions);
                        signature.append(symbol).append(":").append(targetGroupIndex).append("|");
                    }

                    String key = signature.toString();
                    splitter.computeIfAbsent(key, _ -> new HashSet<>()).add(state);
                }
                if (splitter.size() > 1) {
                    changed = true;
                    AppLogger.logger.debug("  Group {} split in: {}", group, splitter.values());
                } else {
                    AppLogger.logger.trace("  Group {} didn't change.", group);
                }
                newPartitions.addAll(splitter.values());
            }
            partitions = newPartitions;
        }

        // Building new minimized DFA. The initial state is the partition list's index
        Map<Integer, Map<String, Integer>> newTrans = new HashMap<>();
        Set<Integer> newFinalStates = new HashSet<>();
        Map<Integer, String> newFinalTags = new HashMap<>();
        String newName = dfa.getTokenName();

        for (int i = 0; i < partitions.size(); i++) {
            Set<Integer> group = partitions.get(i);
            int representative = group.iterator().next();

            if (finalStates.contains(representative)) {
                newFinalStates.add(i);
                if (tags.containsKey(representative)) {
                    newFinalTags.put(i, tags.get(representative));
                }
            }

            // Build transitions
            Map<String, Integer> transitions = new HashMap<>();
            for (String symbol : alphabet) {
                int targetOld = trans.get(representative).getOrDefault(symbol, -1);
                if (targetOld != -1) {
                    int targetNew = findGroupIndex(targetOld, partitions);
                    transitions.put(symbol, targetNew);
                }
            }
            newTrans.put(i, transitions);
        }

        int newStart = findGroupIndex(0, partitions);

        // When the new start of the dfa isn't the 0 position as well, it's required to swap states and treat
        // whether we keep the new state or not
        if (newStart != 0) {
            newTrans = swapStates(newTrans, newStart);

            // Swap Final Sets
            if (newFinalStates.contains(newStart)) {
                newFinalStates.remove(newStart);
                newFinalStates.add(0);
            } else if (newFinalStates.contains(0)) {
                newFinalStates.remove(0);
                newFinalStates.add(newStart);
            }

            // swap Tags
            String tag0 = newFinalTags.remove(0);
            String tagNew = newFinalTags.remove(newStart);
            if (tag0 != null) newFinalTags.put(newStart, tag0);
            if (tagNew != null) newFinalTags.put(0, tagNew);
        }

        AppLogger.logger.debug("   Minimization complete. States: {} -> {}", trans.size(), newTrans.size());
        return new DFA(newName, newFinalStates, newTrans, newFinalTags);
    }

    private static int findGroupIndex(int state, List<Set<Integer>> partitions) {
        if (state == -1) return -1;
        for (int i = 0; i < partitions.size(); i++) {
            if (partitions.get(i).contains(state)) {
                return i;
            }
        }
        throw new IllegalStateException("State " + state + " is missing!");
    }

    /**
     * Swaps state a for b in order to ensure the starting state is 0
     *
     * @param oldMap old transitions map
     * @param a      state a
     * @return the new transition table
     */
    private static Map<Integer, Map<String, Integer>> swapStates(Map<Integer, Map<String, Integer>> oldMap, int a) {
        Map<Integer, Map<String, Integer>> newMap = new HashMap<>();

        for (var entry : oldMap.entrySet()) {
            int key = entry.getKey();
            if (key == a) key = 0;
            else if (key == 0) key = a;

            Map<String, Integer> val = new HashMap<>();
            for (var trans : entry.getValue().entrySet()) {
                int target = trans.getValue();
                if (target == a) target = 0;
                else if (target == 0) target = a;
                val.put(trans.getKey(), target);
            }
            newMap.put(key, val);
        }
        return newMap;
    }
}