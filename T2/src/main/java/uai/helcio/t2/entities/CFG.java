package uai.helcio.t2.entities;

import java.util.*;
import java.util.function.Predicate;

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

    public CFG() {
    }

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

    private <T extends Symbol> void genericAdd(T symbol, List<T> symbols) {
        boolean contains = symbols.stream().
                map(Symbol::hashCode)
                .anyMatch(hash -> hash.equals(symbol.hashCode()));
        if (!contains) {
            symbols.add(symbol);
        }
    }

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

    private void addNonTerminal(NonTerminal nonTerminal) {
        genericAdd(nonTerminal, nonTerminals);
    }

    private void addTerminal(Terminal terminal) {
        genericAdd(terminal, terminals);
    }

    private void addSymbol(Symbol symbol) {
        if (symbol.isTerminal()) {
            addTerminal((Terminal) symbol);
        } else {
            addNonTerminal((NonTerminal) symbol);
        }
    }


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

    public List<List<Symbol>> getProductions(NonTerminal head) {
        return productions.getOrDefault(head, Collections.emptyList());
    }

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

    public NonTerminal getAugmentedRoot() {
        return augmentedRoot != null ? augmentedRoot : root;
    }
}
