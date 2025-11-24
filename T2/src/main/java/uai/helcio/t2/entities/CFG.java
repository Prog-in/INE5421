package uai.helcio.t2.entities;

import java.util.*;
import java.util.function.Predicate;

/**
 * Represents a Context-Free Grammar (CFG).
 * <p>
 * This class serves as the central repository for the grammar rules,
 * terminals, and non-terminals. It includes logic to compute the fundamental
 * sets required for parsing algorithms and provides functionality
 * to augment the grammar for LR parsing.
 * </p>
 */
public class CFG {
    private final Map<NonTerminal, List<List<Symbol>>> productions = new HashMap<>();
    private final List<NonTerminal> nonTerminals = new ArrayList<>();
    private final List<Terminal> terminals = new ArrayList<>();
    private NonTerminal root;

    private final Map<Symbol, List<Terminal>> first = new HashMap<>();
    private final Map<Symbol, List<Terminal>> follow = new HashMap<>();
    private boolean computedFirst = false;
    private boolean computedFollow = false;
    private NonTerminal augmentedRoot;

    /**
     * Default constructor for initializing an empty Control-Free Grammar.
     */
    public CFG() {
    }

    /**
     * Returns a string representation of the grammar rules.
     * <p>
     * The format follows the standard notation: <code>Head -> Body1 | Body2</code>.
     * </p>
     *
     * @return A formatted string containing all productions.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        productions.forEach((k, lists) -> {
            sb.append(k).append(" -> ");
            lists.forEach(list -> {
                list.forEach(sb::append);
                sb.append(" | ");
            });
            sb.delete(sb.length() - 2, sb.length());
            sb.append(System.lineSeparator());
        });
        return sb.toString();
    }

    /**
     * Adds a single symbol to a list if it is not already present.
     *
     * @param symbol  The symbol to be added.
     * @param symbols The target list.
     * @param <T>     A generic class which extends {@link Symbol} (e.g., {@link Terminal} or {@link NonTerminal}).
     */
    private <T extends Symbol> void genericAdd(T symbol, List<T> symbols) {
        boolean contains = symbols.stream().
                map(Symbol::hashCode)
                .anyMatch(hash -> hash.equals(symbol.hashCode()));
        if (!contains) {
            symbols.add(symbol);
        }
    }

    /**
     * Generically adds all symbols from a source list that aren't contained in the target symbol list.
     *
     * @param toInsert The list of symbols to be inserted.
     * @param symbols  The target list of symbols to merge into.
     * @param <T>      A generic class which extends {@link Symbol}.
     * @return {@code true} if the target list changed as a result of the call.
     */
    private <T extends Symbol> boolean genericAddAll(List<T> toInsert, List<T> symbols) {
        List<Integer> hashes = symbols.stream().map(Symbol::hashCode).toList();
        boolean someAdded = false;
        for (T symbol : toInsert) {
            boolean contains = hashes.stream().anyMatch(hash -> hash.equals(symbol.hashCode()));
            if (!contains) {
                someAdded = true;
                symbols.add(symbol);
            }
        }
        return someAdded;
    }

    /**
     * Registers a NonTerminal in the grammar's list.
     *
     * @param nonTerminal The non-terminal to add.
     */
    private void addNonTerminal(NonTerminal nonTerminal) {
        genericAdd(nonTerminal, nonTerminals);
    }

    /**
     * Registers a Terminal in the grammar's list.
     *
     * @param terminal The terminal to add.
     */
    private void addTerminal(Terminal terminal) {
        genericAdd(terminal, terminals);
    }

    /**
     * Dispatches the addition of a symbol to the appropriate list.
     *
     * @param symbol The symbol to add.
     */
    private void addSymbol(Symbol symbol) {
        if (symbol.isTerminal()) {
            addTerminal((Terminal) symbol);
        } else {
            addNonTerminal((NonTerminal) symbol);
        }
    }

    /**
     * Adds a new production rule to the grammar.
     * <p>
     * If the head of the production does not exist, it is created.
     * If this is the first production added, the head becomes the root of the grammar.
     * </p>
     *
     * @param headRepr The string representation of the production head.
     * @param body     The list of symbols representing the production body.
     */
    public void addProduction(String headRepr, List<Symbol> body) {
        NonTerminal head = NonTerminal.of(headRepr);
        // the head of the first production is the root
        if (root == null) {
            root = head;
        }
        List<List<Symbol>> prods = Optional.ofNullable(productions.get(head)).orElseGet(ArrayList::new);
        prods.add(body);
        addNonTerminal(head);
        body.forEach(this::addSymbol);
        productions.putIfAbsent(head, prods);
    }

    /**
     * Retrieves all production bodies associated with a specific NonTerminal head.
     *
     * @param head The NonTerminal symbol.
     * @return A list of production bodies, or an empty list if the head has no productions.
     */
    public List<List<Symbol>> getProductions(NonTerminal head) {
        return productions.getOrDefault(head, Collections.emptyList());
    }



    /**
     * Computes the FIRST set for all symbols in the grammar.
     */
    public void getFirst() {
        if (computedFirst) {
            return;
        }

        for (Terminal terminal : terminals) {
            first.put(terminal, new ArrayList<>(Collections.singletonList(terminal)));
        }

        nonTerminals.forEach(x -> first.put(x, new ArrayList<>()));
        boolean someAdded;
        do {
            someAdded = false;
            for (NonTerminal nonTerminal : nonTerminals) {
                List<Terminal> fst = getFirst(nonTerminal);
                someAdded |= genericAddAll(fst, first.get(nonTerminal));
            }
        } while (someAdded);
        computedFirst = true;
    }

    /**
     * Helper method to compute the FIRST set for a specific NonTerminal based on its productions.
     *
     * @param symbol The NonTerminal symbol to analyze.
     * @return A list of Terminals belonging to the FIRST set of the symbol.
     */
    private List<Terminal> getFirst(NonTerminal symbol) {
        List<Terminal> fst = new ArrayList<>();
        List<List<Symbol>> prods = productions.get(symbol);
        for (List<Symbol> prod : prods) {
            boolean allNullable = true;
            for (Symbol s : prod) {
                List<Terminal> fstProd = first.get(s);
                boolean isNullable = fstProd.contains(Terminal.EPSILON);
                fstProd.stream()
                        .filter(Predicate.not(Predicate.isEqual(Terminal.EPSILON)))
                        .forEach(t -> genericAdd(t, fst));
                if (!isNullable) {
                    allNullable = false;
                    break;
                }
            }
            if (allNullable) {
                genericAdd(Terminal.EPSILON, fst);
            }
        }
        return fst;
    }

    /**
     * Computes the FOLLOW set for all NonTerminals in the grammar.
     *
     * @return The map containing the FOLLOW sets for every symbol.
     */
    public Map<Symbol, List<Terminal>> getFollow() {
        if (computedFollow) {
            return follow;
        }

        for (Terminal terminal : terminals) {
            follow.put(terminal, new ArrayList<>(Collections.singletonList(terminal)));
        }

        nonTerminals.forEach(x -> follow.put(x, new ArrayList<>()));
        genericAdd(Terminal.END, follow.get(root));
        boolean someAdded;
        do {
            someAdded = false;
            for (NonTerminal nonTerminal : nonTerminals) {
                List<Terminal> fst = getFollow(nonTerminal);
                someAdded |= genericAddAll(fst, follow.get(nonTerminal));
            }
        } while (someAdded);
        computedFollow = true;
        return follow;
    }

    /**
     * Helper method to compute the FOLLOW set for a specific NonTerminal.
     * <p>
     * It scans all productions to find occurrences of the symbol on the Right-Hand Side
     * and propagates lookahead terminals accordingly.
     * </p>
     *
     * @param symbol The symbol to analyze.
     * @return A list of Terminals for the FOLLOW set.
     */
    private List<Terminal> getFollow(NonTerminal symbol) {
        List<Terminal> flw = new ArrayList<>();
        for (var production : productions.entrySet()) {
            NonTerminal head = production.getKey();
            List<List<Symbol>> body = production.getValue();
            for (List<Symbol> prod : body) {
                for (Symbol s : prod.reversed().stream().filter(Predicate.not(Symbol::isTerminal)).toList()) {
                    genericAddAll(follow.get(head), follow.get(s));
                    if (!first.get(s).contains(Terminal.EPSILON)) {
                        break;
                    }
                }
                Iterator<Symbol> prodIter = prod.iterator();
                while (prodIter.hasNext()) {
                    Symbol s = prodIter.next();
                    if (s.equals(symbol)) {
                        while (prodIter.hasNext()) {
                            Symbol next = prodIter.next();
                            List<Terminal> fstNext = first.get(next);
                            boolean isNullable = fstNext.contains(Terminal.EPSILON);
                            fstNext.stream()
                                    .filter(Predicate.not(Predicate.isEqual(Terminal.EPSILON)))
                                    .forEach(t -> genericAdd(t, flw));
                            if (!isNullable) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return flw;
    }



    /**
     * Augments the grammar by creating a new start symbol and a production pointing to the original root.
     * <p>
     * For an original root <code>S</code>, this creates <code>S' -> S</code>.
     * </p>
     *
     * @throws IllegalStateException If the grammar is empty
     */
    public void augment() {
        if (root == null) throw new IllegalStateException("Gramática vazia");

        if (augmentedRoot != null) return;

        String rootRepr = root.getRepr() + "'";
        augmentedRoot = NonTerminal.of(rootRepr);

        List<Symbol> body = new ArrayList<>();
        body.add(root);

        List<List<Symbol>> prods = new ArrayList<>();
        prods.add(body);
        productions.put(augmentedRoot, prods);

        addNonTerminal(augmentedRoot);

        computedFirst = false;
        computedFollow = false;
        first.clear();
        follow.clear();
    }

    /**
     * Retrieves the effective root of the grammar.
     *
     * @return The augmented root if {@link #augment()} has been called, otherwise the original root.
     */
    public NonTerminal getRoot() {
        return augmentedRoot != null ? augmentedRoot : root;
    }
}