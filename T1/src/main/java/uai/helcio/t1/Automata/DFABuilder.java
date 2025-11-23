package uai.helcio.t1.Automata;


import uai.helcio.t1.entities.RegexNode;
import uai.helcio.t1.entities.RegexTree;
import uai.helcio.utils.AppLogger;

import java.util.*;

public class DFABuilder {

    /**
     * Builds an automata from an existing regex
     * @param tree regexTree
     * @return the automata
     */
    public static DFA build(RegexTree tree) {
        AppLogger.logger.debug("   Starting DFA building for: {}", tree.getTreeName());

        Set<Integer> startSet = tree.getRoot().getFirstpos();
        Map<Set<Integer>, Integer> dStates = new HashMap<>();
        Queue<Set<Integer>> unmarkedStates = new LinkedList<>();
        Map<Integer, Map<String, Integer>> dTran = new HashMap<>();

        int stateCounter = 0;
        dStates.put(startSet, stateCounter);
        unmarkedStates.add(startSet);
        dTran.put(stateCounter, new HashMap<>());

        Set<String> alphabet = tree.getAlphabet();

        AppLogger.logger.trace("Initial State (0): {}", startSet);

        while (!unmarkedStates.isEmpty()) {
            Set<Integer> T = unmarkedStates.poll();
            int tID = dStates.get(T);

            AppLogger.logger.trace("Processing state {}: set {}", tID, T);

            for (String a : alphabet) {
                Set<Integer> U = new HashSet<>();
                for (int p : T) {
                    if (a.equals(tree.getSymbol(p))) {
                        U.addAll(tree.getFollowpos().get(p));
                    }
                }

                if (!U.isEmpty()) {
                    if (!dStates.containsKey(U)) {
                        stateCounter++;
                        dStates.put(U, stateCounter);
                        unmarkedStates.add(U);
                        dTran.put(stateCounter, new HashMap<>());
                        AppLogger.logger.debug("  Found new State ID {}: {} from symbol '{}'", stateCounter, U, a);
                    }

                    int uID = dStates.get(U);
                    dTran.get(tID).put(a, uID);
                    // LOG
                    AppLogger.logger.trace("  Transition built: {} --({})--> {}", tID, a, uID);
                }
            }
        }

        Set<Integer> finalStates = new HashSet<>(); // if contains #

        int endPosId = -1;
        for (int i = 1; ; i++) {
            String sym = tree.getSymbol(i);
            if (sym == null) break;
            if (RegexNode.END_NODE_SYMBOL.equals(sym)) {
                endPosId = i;
                break;
            }
        }

        for (Map.Entry<Set<Integer>, Integer> entry : dStates.entrySet()) {
            Set<Integer> positions = entry.getKey();
            int stateId = entry.getValue();

            if (positions.contains(endPosId)) {
                finalStates.add(stateId);
            }
        }

        AppLogger.logger.debug("   DFA Built. Total states: {}", dStates.size());
        return new DFA(tree.getTreeName(), finalStates, dTran);
    }
}