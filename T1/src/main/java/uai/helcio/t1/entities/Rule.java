package uai.helcio.t1.entities;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record Rule(String name, String regex) {

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return String.format("Rule [ name=%s, regex=%s ]", name, regex);
    }
}
