package uai.helcio.t2.entities;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public record Item(NonTerminal head, List<Symbol> body, int dotPosition) {

    public Symbol getSymbolAfterDot() {
        if (dotPosition >= body.size()) {
            return null;
        }
        Symbol s = body.get(dotPosition);
        if (s.equals(Terminal.EPSILON)) {
            return null;
        }
        return s;
    }

    public Item advance() {
        if (getSymbolAfterDot() == null && !isEpsilonProduction()) {
            throw new IllegalStateException("Cannot advance: item already completed");
        }
        return new Item(head, body, dotPosition + 1);
    }

    public boolean isReduce() {
        if (isEpsilonProduction()) return true;
        return dotPosition == body.size();
    }

    private boolean isEpsilonProduction() {
        return body.size() == 1 && body.getFirst().equals(Terminal.EPSILON);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item(NonTerminal head1, List<Symbol> body1, int position))) return false;
        return dotPosition == position &&
                Objects.equals(head, head1) &&
                Objects.equals(body, body1);
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(head.getRepr()).append(" -> ");
        if (isEpsilonProduction()) {
            sb.append("• (ε)");
        } else {
            for (int i = 0; i < body.size(); i++) {
                if (i == dotPosition) sb.append("• ");
                sb.append(body.get(i).getRepr()).append(" ");
            }
            if (dotPosition == body.size()) sb.append("•");
        }
        return sb.toString();
    }
}