package uai.helcio.t1.Automata;


import uai.helcio.utils.AppLogger;

import java.util.List;
import java.util.Map;

public class NFAUnionBuilder {

    public static NFA union(List<DFA> dfas) {
        AppLogger.logger.debug("   Unifying automatas");

        NFA nfa = new NFA();

        // state 0 is the global starter state, so initializes with 1
        int stateOffset = 1;

        for (DFA dfa : dfas) {
            AppLogger.logger.trace("  Integrating DFA '{}' with offset {}", dfa.getTokenName(), stateOffset);
            
            // adding epsilon transition from the global start to the automata start
            nfa.addEpsilonTransition(NFA.START_STATE, stateOffset);

            // Copying all transitions but renaming states
            Map<Integer, Map<String, Integer>> transition = dfa.getTransitionTable();

            for (Map.Entry<Integer, Map<String, Integer>> state : transition.entrySet()) {
                int oldState = state.getKey();
                int newState = oldState + stateOffset;

                for (Map.Entry<String, Integer> transitionEntry : state.getValue().entrySet()) {
                    String symbol = transitionEntry.getKey();
                    int oldTarget = transitionEntry.getValue();
                    int newTarget = oldTarget + stateOffset;

                    nfa.addTransition(newState, symbol, newTarget);
                }
            }

            // Mapping final states
            for (Integer oldFinal : dfa.getFinalStates()) {
                int newFinal = oldFinal + stateOffset;
                nfa.addFinalState(newFinal, dfa.getTokenName());
            }

            // Updating the offset to evaluate the next DFA. We must have the largest id + 1
            int maxStateId = transition.keySet().stream().max(Integer::compareTo).orElse(0);
            stateOffset += (maxStateId + 1);
        }

        AppLogger.logger.debug("   Union complete. Resulting NFA: {}", nfa);
        return nfa;
    }
}