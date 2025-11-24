package uai.helcio.t2.table;

import org.jetbrains.annotations.NotNull;
import uai.helcio.t2.entities.Item;

public record TableEntry(ActionType type, int targetState, Item productionToReduce) {

    public static TableEntry shift(int state) {
        return new TableEntry(ActionType.SHIFT, state, null);
    }

    public static TableEntry reduce(Item item) {
        return new TableEntry(ActionType.REDUCE, -1, item);
    }

    public static TableEntry accept() {
        return new TableEntry(ActionType.ACCEPT, -1, null);
    }

    @Override
    public @NotNull String toString() {
        return switch (type) {
            case SHIFT -> "s" + targetState;
            case REDUCE -> "r(" + productionToReduce.head() + "->" + productionToReduce.body() + ")";
            case ACCEPT -> "acc";
            case ERROR -> "err";
        };
    }
}