package uai.helcio.entities;

public class Rule {
    private final String name;
    private final String regex;

    public Rule(String ruleName, String pureRegex) {
        this.name = ruleName;
        this.regex = pureRegex;
    }

    @Override
    public String toString() {
        return String.format("Rule [ name=%s, regex=%s ]", name, regex);
    }

    public String getName() {
        return name;
    }

    public String getRegex() {
        return regex;
    }
}
