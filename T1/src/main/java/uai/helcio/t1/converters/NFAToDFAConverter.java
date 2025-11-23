package uai.helcio.t1.converters;

import uai.helcio.t1.Automata.DFA;
import uai.helcio.t1.Automata.NFA;
import uai.helcio.utils.AppLogger;

import java.util.*;

public class NFAToDFAConverter {

    public static DFA convert(NFA nfa, List<String> rulePriority) {
        AppLogger.logger.debug("    Starting Determinization: NFA -> DFA");

        Map<Set<Integer>, Integer> dStates = new HashMap<>();
        Queue<Set<Integer>> unmarkedStates = new LinkedList<>();
        Map<Integer, Map<String, Integer>> dTran = new HashMap<>();
        Map<Integer, String> finalStateTags = new HashMap<>();
        Set<Integer> finalStates = new HashSet<>();

        Set<Integer> startSet = epsilonClosure(nfa, Set.of(NFA.START_STATE));

        int stateCounter = 0;
        dStates.put(startSet, stateCounter);
        unmarkedStates.add(startSet);
        dTran.put(stateCounter, new HashMap<>());

         AppLogger.logger.trace("    Initial State of DFA: {}", startSet);

        while (!unmarkedStates.isEmpty()) {
            Set<Integer> T = unmarkedStates.poll();
            int tID = dStates.get(T);

            String tokenTag = resolveTokenPriority(nfa, T, rulePriority);
            if (tokenTag != null) {
                finalStates.add(tID);
                finalStateTags.put(tID, tokenTag);
            }

            for (String symbol : nfa.getAlphabet()) {
                Set<Integer> moveResult = new HashSet<>();
                for (int nfaState : T) {
                    Set<Integer> targets = nfa.getTransitions(nfaState).get(symbol);
                    if (targets != null) {
                        moveResult.addAll(targets);
                    }
                }

                Set<Integer> U = epsilonClosure(nfa, moveResult);

                if (!U.isEmpty()) {
                    if (!dStates.containsKey(U)) {
                        stateCounter++;
                        dStates.put(U, stateCounter);
                        unmarkedStates.add(U);
                        dTran.put(stateCounter, new HashMap<>());
                        AppLogger.logger.trace("    DFA New State {}: {} (via '{}')", stateCounter, U, symbol);
                    }
                    int uID = dStates.get(U);
                    dTran.get(tID).put(symbol, uID);
                }
            }
        }

        AppLogger.logger.debug("Final DFA with {} states.", dStates.size());
        return new DFA("LEXICAL_ANALYZER", finalStates, dTran, finalStateTags);
    }

    private static Set<Integer> epsilonClosure(NFA nfa, Set<Integer> states) {
        Stack<Integer> stack = new Stack<>();
        Set<Integer> closure = new HashSet<>(states);
        stack.addAll(states);

        while (!stack.isEmpty()) {
            int t = stack.pop();
            Set<Integer> epsTargets = nfa.getEpsilonTransitions(t);

            for (int u : epsTargets) {
                if (!closure.contains(u)) {
                    closure.add(u);
                    stack.push(u);
                }
            }
        }
        return closure;
    }

    private static String resolveTokenPriority(NFA nfa, Set<Integer> dfaStateSet, List<String> rulePriority) {
        String bestToken = null;
        int bestIndex = Integer.MAX_VALUE;

        for (int nfaState : dfaStateSet) {
            String token = nfa.getFinalStateTokens().get(nfaState);
            if (token != null) {
                int index = rulePriority.indexOf(token);

                // if it's in the list and has priority
                if (index != -1 && index < bestIndex) {
                    bestIndex = index;
                    bestToken = token;
                }
            }
        }
        return bestToken;
    }
}