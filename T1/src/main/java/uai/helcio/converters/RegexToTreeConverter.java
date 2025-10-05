package uai.helcio.converters;

import uai.helcio.entities.RegexTree;
import uai.helcio.entities.RegexNode.BinaryNode;
import uai.helcio.entities.RegexNode;
import uai.helcio.entities.RegexNode.UnaryNode;
import uai.helcio.entities.Rule;

/**
 * Precedence order:
 * 1. |
 * 2. .
 * 3. *, +, ?
 */
public class RegexToTreeConverter {
    private final String regex;
    private int pos = 0;

    private RegexToTreeConverter(String regex) {
        this.regex = regex;
    }

    public static RegexTree convert(Rule rule) {
        RegexToTreeConverter parser = new RegexToTreeConverter(rule.getRegex());
        RegexNode leftSubTree = parser.parseRegex(null);
        return new RegexTree(rule.getName(), leftSubTree);
    }

    private RegexNode parseRegex(RegexNode parent) {
        return parseUnion(parent);
    }

    private RegexNode parseUnion(RegexNode parent) {
        RegexNode left = parseConcat(parent);
        while (peek() == '|') {
            next();
            RegexNode right = parseConcat(parent);
            BinaryNode op = new BinaryNode(parent, "|");
            left = linkBinary(op, left, right);
        }
        return left;
    }

    private RegexNode parseConcat(RegexNode parent) {
        RegexNode left = parseRepeat(parent);
        while (hasNext() && peek() != ')' && peek() != '|') {
            RegexNode right = parseRepeat(parent);
            BinaryNode op = new BinaryNode(parent, ".");
            left = linkBinary(op, left, right);
        }
        return left;
    }

    private RegexNode parseRepeat(RegexNode parent) {
        RegexNode node = parseBase(parent);
        while (peek() == '*' || peek() == '+' || peek() == '?') {
            char opSymbol = next();
            UnaryNode op = new UnaryNode(parent, String.valueOf(opSymbol));
            op.kid = node;
            node = op;
        }
        return node;
    }

    private RegexNode parseBase(RegexNode parent) {
        if (peek() == '(') {
            next(); // consume '('
            RegexNode inside = parseRegex(parent);
            expect(')');
            return inside;
        }
        char c = next();
        return new RegexNode(String.valueOf(c), parent);
    }

    private boolean hasNext() {
        return pos < regex.length();
    }

    private char peek() {
        return hasNext() ? regex.charAt(pos) : '\0';
    }

    private char next() {
        return regex.charAt(pos++);
    }

    private void expect(char expected) {
        if (!hasNext() || regex.charAt(pos) != expected) {
            throw new IllegalStateException(String.format("Expected '%s' at position %s", expected, pos));
        }
        pos++;
    }

    private BinaryNode linkBinary(BinaryNode op, RegexNode left, RegexNode right) {
        op.left = left;
        op.right = right;
        left.parent = op;
        right.parent = op;
        return op;
    }
}
