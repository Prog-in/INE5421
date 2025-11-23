package uai.helcio.t1.entities;

public record Rule(String name, String regex) {

    @Override
    public String toString() {
        return String.format("Rule [ name=%s, regex=%s ]", name, regex);
    }
}
