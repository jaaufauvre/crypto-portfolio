package org.example.math;

import java.math.BigDecimal;

public class NumberUtils {
    public static String withPlusOrMinusPrefix(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) > 0 ? "+" + value.toPlainString() : value.toPlainString();
    }
}
