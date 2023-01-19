package org.parser.app.service.std;

import lombok.NonNull;

import java.util.function.LongBinaryOperator;
import java.util.function.LongPredicate;

public enum PredicateOperation {

    GT(">", (v1, v2) -> v2 - v1, v -> v > 0),

    LT("<", (v1, v2) -> v2 - v1, v -> v < 0),

    GTE(">=", (v1, v2) -> v2 - v1, v -> v >= 0),

    LTE("<=", (v1, v2) -> v2 - v1, v -> v <= 0);

    private final String alias;
    private final LongBinaryOperator operator;
    private final LongPredicate predicate;

    PredicateOperation(
            @NonNull String alias,
            @NonNull LongBinaryOperator operator,
            @NonNull LongPredicate predicate) {
        this.alias = alias;
        this.operator = operator;
        this.predicate = predicate;
    }

    public boolean compute(final long arg1, final long arg2) {
        return this.predicate.test(this.operator.applyAsLong(arg1, arg2));
    }

    @NonNull
    public static PredicateOperation value(@NonNull String operationStr) {
        for (final PredicateOperation op : values()) {
            if (op.alias.equals(operationStr)) {
                return op;
            }
        }

        throw new IllegalArgumentException(operationStr);
    }
}
