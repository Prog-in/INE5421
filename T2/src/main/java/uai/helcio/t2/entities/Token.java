package uai.helcio.t2.entities;

import org.jetbrains.annotations.NotNull;

public record Token(String type, String attribute) {

    @Override
    public @NotNull String toString() {
        return String.format("<%s, %s>", type, attribute);
    }
}