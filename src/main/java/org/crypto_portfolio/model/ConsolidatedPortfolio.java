package org.crypto_portfolio.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Objects;

import static org.crypto_portfolio.math.NumberUtils.withPlusOrMinusPrefix;

public class ConsolidatedPortfolio extends ArrayList<ConsolidatedAsset> {

    public static final String UNKNOWN = "Unknown";

    private BigDecimal getTotalPaidDecimal() {
        if (stream().map(ConsolidatedAsset::getTotalPaidDecimal).allMatch(Objects::isNull)) {
            return null;
        }
        return stream()
            // Ignore assets for which the current price is unknown
            .filter(asset -> !asset.isUnitPriceUnknown())
            .map(ConsolidatedAsset::getTotalPaidDecimal)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getTotalPaid() {
        BigDecimal totalPaidDecimal = getTotalPaidDecimal();
        if (totalPaidDecimal == null) {
            return UNKNOWN;
        }
        return totalPaidDecimal
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private BigDecimal getTotalCurrentBalanceDecimal() {
        if (stream().map(ConsolidatedAsset::getCurrentBalanceDecimal).allMatch(Objects::isNull)) {
            return null;
        }
        return stream()
                .map(ConsolidatedAsset::getCurrentBalanceDecimal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getTotalCurrentBalance() {
        BigDecimal totalCurrentBalanceDecimal = getTotalCurrentBalanceDecimal();
        if (totalCurrentBalanceDecimal == null) {
            return UNKNOWN;
        }
        return totalCurrentBalanceDecimal
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private BigDecimal getTotalAbsoluteDeltaDecimal() {
        if (stream().map(ConsolidatedAsset::getAbsoluteDeltaDecimal).allMatch(Objects::isNull)) {
            return null;
        }
        return stream()
                .map(ConsolidatedAsset::getAbsoluteDeltaDecimal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getTotalAbsoluteDelta() {
        BigDecimal totalAbsoluteDeltaDecimal = getTotalAbsoluteDeltaDecimal();
        if (totalAbsoluteDeltaDecimal == null) {
            return UNKNOWN;
        }
        BigDecimal absoluteDelta = totalAbsoluteDeltaDecimal
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return withPlusOrMinusPrefix(absoluteDelta);
    }

    private BigDecimal getPercentageChangeDecimal() {
        BigDecimal currentBalanceDecimal = getTotalCurrentBalanceDecimal();
        if (currentBalanceDecimal == null) {
            return null;
        }
        BigDecimal totalPaidDecimal = getTotalPaidDecimal();
        if (BigDecimal.ZERO.compareTo(totalPaidDecimal) == 0) {
            return null;
        }
        return currentBalanceDecimal
                .subtract(totalPaidDecimal)
                .divide(totalPaidDecimal, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public String getTotalPercentageChange() {
        BigDecimal percentageChangeDecimal = getPercentageChangeDecimal();
        if (percentageChangeDecimal == null) {
            return UNKNOWN;
        }
        BigDecimal percentageChange = percentageChangeDecimal
                .setScale(0, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return withPlusOrMinusPrefix(percentageChange) + "%";
    }
}
