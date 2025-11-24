package uai.helcio.t2.generators;

import uai.helcio.t2.entities.*;
import uai.helcio.t2.table.ActionType;
import uai.helcio.t2.table.TableEntry;
import uai.helcio.utils.AppLogger;

import java.util.*;

public class SLRGenerator {

    private final CFG cfg;
    private final Map<Integer, Map<Symbol, TableEntry>> parsingTable = new HashMap<>();
    private final List<Set<Item>> canonicalCollection = new ArrayList<>();

    public SLRGenerator(CFG cfg) {
        this.cfg = cfg;
    }

    public Map<Integer, Map<Symbol, TableEntry>> generate() {
        cfg.augment();
        cfg.getFirst();
        cfg.getFollow();
        buildCanonicalCollection();
        buildTable();
        printTable();
        return parsingTable;
    }


    private Set<Item> closure(Set<Item> items) {
        Set<Item> closureSet = new HashSet<>(items);
        boolean changed;
        do {
            changed = false;
            Set<Item> tempItems = new HashSet<>();

            for (Item item : closureSet) {
                Symbol B = item.getSymbolAfterDot();

                // A -> alpha . B beta
                if (B != null && B.isNonTerminal()) {
                    NonTerminal nonTerminalB = (NonTerminal) B;
                    //  B -> gamma
                    for (List<Symbol> body : cfg.getProductions(nonTerminalB)) {
                        // add B -> . gamma
                        Item newItem = new Item(nonTerminalB, body, 0);
                        if (!closureSet.contains(newItem) && !tempItems.contains(newItem)) {
                            tempItems.add(newItem);
                            changed = true;
                        }
                    }
                }
            }
            closureSet.addAll(tempItems);
        } while (changed);
        return closureSet;
    }

    private Set<Item> goTo(Set<Item> i, Symbol x) {
        Set<Item> movedItems = new HashSet<>();
        for (Item item : i) {
            Symbol postDot = item.getSymbolAfterDot();
            if (Objects.equals(postDot, x)) {
                movedItems.add(item.advance());
            }
        }
        return closure(movedItems);
    }


    private void buildCanonicalCollection() {
        // Closure({S' -> . S})
        NonTerminal startParams = cfg.getAugmentedRoot();
        List<Symbol> startBody = cfg.getProductions(startParams).getFirst();

        Item initialItem = new Item(startParams, startBody, 0);
        Set<Item> initialSet = closure(Collections.singleton(initialItem));

        canonicalCollection.add(initialSet);

        boolean changed;
        do {
            changed = false;
            int currentSize = canonicalCollection.size();
            for (int i = 0; i < currentSize; i++) {
                Set<Item> currentSet = canonicalCollection.get(i);

                Set<Symbol> symbolsAfterDot = new HashSet<>();
                for(Item item : currentSet) {
                    if (item.getSymbolAfterDot() != null) {
                        symbolsAfterDot.add(item.getSymbolAfterDot());
                    }
                }

                for (Symbol x : symbolsAfterDot) {
                    Set<Item> nextSet = goTo(currentSet, x);
                    if (!nextSet.isEmpty() && !canonicalCollection.contains(nextSet)) {
                        canonicalCollection.add(nextSet);
                        changed = true;
                    }
                }
            }
        } while (changed);

        AppLogger.logger.info("Canonical Collection generated. Total states: {}", canonicalCollection.size());
    }


    private void buildTable() {
        for (int i = 0; i < canonicalCollection.size(); i++) {
            Set<Item> stateItems = canonicalCollection.get(i);
            parsingTable.putIfAbsent(i, new HashMap<>());

            for (Item item : stateItems) {
                // A -> alpha . a beta (Shift)
                Symbol a = item.getSymbolAfterDot();
                if (a != null && a.isTerminal()) {
                    Set<Item> nextStateItems = goTo(stateItems, a);
                    int nextStateIndex = canonicalCollection.indexOf(nextStateItems);

                    if (nextStateIndex != -1) {
                        addEntry(i, a, TableEntry.shift(nextStateIndex));
                    }
                }

                // A -> alpha . (Reduce)
                if (item.isReduce()) {
                    // A = S',  ACCEPT
                    if (item.head().equals(cfg.getAugmentedRoot())) {
                        addEntry(i, Terminal.END, TableEntry.accept());
                    } else {
                        // REDUCE for all 'a' in Follow(A)
                        List<Terminal> followA = cfg.getFollow().get(item.head());
                        if (followA != null) {
                            for (Terminal t : followA) {
                                addEntry(i, t, TableEntry.reduce(item));
                            }
                        }
                    }
                }
            }

            Set<Symbol> allSymbols = new HashSet<>();
            stateItems.forEach(it -> {
                if(it.getSymbolAfterDot() != null) allSymbols.add(it.getSymbolAfterDot());
            });

            for(Symbol s : allSymbols) {
                if (s.isNonTerminal()) {
                    Set<Item> nextSet = goTo(stateItems, s);
                    int nextStateIndex = canonicalCollection.indexOf(nextSet);
                    if (nextStateIndex != -1) {
                        addEntry(i, s, TableEntry.shift(nextStateIndex));
                    }
                }
            }
        }
    }

    private void addEntry(int state, Symbol symbol, TableEntry entry) {
        Map<Symbol, TableEntry> row = parsingTable.get(state);

        if (row.containsKey(symbol)) {
            TableEntry existing = row.get(symbol);

            if (existing.equals(entry)) return;

            AppLogger.logger.warn("CONFLITO no estado {}, símbolo {}: {} vs {}", state, symbol, existing, entry);

            // Conflict resolution

            // Shift/Reduce -> Shift
            if (existing.type() == ActionType.SHIFT && entry.type() == ActionType.REDUCE) {
                AppLogger.logger.warn(" -> Resolvido mantendo SHIFT (Padrão)");
                return;
            }
            if (existing.type() == ActionType.REDUCE && entry.type() == ActionType.SHIFT) {
                AppLogger.logger.warn(" -> Resolvido trocando para SHIFT (Padrão)");
                row.put(symbol, entry); // overrides
                return;
            }

            // Reduce/Reduce -> keeps the first entry
            if (existing.type() == ActionType.REDUCE && entry.type() == ActionType.REDUCE) {
                AppLogger.logger.warn(" -> Resolvido mantendo a primeira redução encontrada.");
                return;
            }
        }
        row.put(symbol, entry);
    }

    private void printTable() {
        StringBuilder sb = new StringBuilder("\n--- SLR Parsing Table ---\n");
        List<Integer> sortedStates = new ArrayList<>(parsingTable.keySet());
        Collections.sort(sortedStates);

        for (Integer state : sortedStates) {
            sb.append("State ").append(state).append(":\n");
            Map<Symbol, TableEntry> row = parsingTable.get(state);
            row.forEach((sym, action) ->
                    sb.append("  On ")
                            .append(sym.getRepr())
                            .append(" -> ")
                            .append(action)
                            .append("\n"));
        }
        AppLogger.logger.info(sb.toString());
    }

}