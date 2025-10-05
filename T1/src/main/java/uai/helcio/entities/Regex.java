package uai.helcio.entities;

import org.apache.commons.collections4.ListUtils;

import java.util.List;
import java.util.function.Predicate;

public class Regex {

    public static class Operation {
        public static final Operation OR = new Operation('|');
        public static final Operation STAR = new Operation('*');
        public static final Operation CAT = new Operation('.');

        private final char repr;

        Operation(char repr) {
            this.repr = repr;
        }

        public final char getRepr() {
            return repr;
        }

        public static List<Operation> values() {
            return List.of(OR, STAR, CAT);
        }

        public static boolean isOperation(char repr) {
            return Operation.values().stream()
                    .map(Operation::getRepr)
                    .anyMatch(Predicate.isEqual(repr));
        }
    }

    public static class ExtendedOperation extends Operation {
        public static final ExtendedOperation QUESTION_MARK = new ExtendedOperation('?');
        public static final ExtendedOperation PLUS_SIGN = new ExtendedOperation('+');

        ExtendedOperation(char repr) {
            super(repr);
        }

        public static List<Operation> values() {
            List<Operation> pureOperations = Operation.values();
            List<ExtendedOperation> extendedOperations = List.of(QUESTION_MARK, PLUS_SIGN);
            return ListUtils.union(pureOperations, extendedOperations);
        }

        public static boolean isOperation(char repr) {
            return ExtendedOperation.values().stream()
                    .map(Operation::getRepr)
                    .anyMatch(Predicate.isEqual(repr));
        }
    }
}
