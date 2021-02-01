package org.crypto_portfolio.model;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.crypto_portfolio.math.NumberUtils.withPlusOrMinusPrefix;

public class ConsolidatedAsset implements Comparable<ConsolidatedAsset> {

    private static final String UNKNOWN = "Unknown";
    private static final String IGNORED = "Ignored";
    private static final String NON_APPLICABLE = "n/a";

    private final Asset asset;
    private final BigDecimal currentUnitPriceInEuros;
    private final BigDecimal amountDecimal;
    private final BigDecimal totalPaidDecimal;
    private final BigDecimal currentBalanceDecimal;
    private final BigDecimal absoluteDeltaDecimal;
    private final BigDecimal percentageChangeDecimal;

    public ConsolidatedAsset(Asset asset, String currentUnitPriceInEuros) {
        this.asset = asset;
        this.currentUnitPriceInEuros = parseOrNull(currentUnitPriceInEuros);
        this.amountDecimal = computeAmount();
        this.totalPaidDecimal = computeTotalPaid();
        this.currentBalanceDecimal = computeCurrentBalanceDecimal();
        this.absoluteDeltaDecimal = computeAbsoluteDeltaDecimal();
        this.percentageChangeDecimal = computePercentageChangeDecimal();
    }

    private static BigDecimal parseOrNull(String number) {
        try {
            return new BigDecimal(number);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isUnitPriceUnknown() {
        return currentUnitPriceInEuros == null;
    }

    public String getName() {
        return String.format("%s", asset.id);
    }

    private BigDecimal computeAmount() {
        return asset.getPurchases().stream()
                .map(p -> new BigDecimal(p.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getAmount() {
        return amountDecimal
                .setScale(8, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private BigDecimal computeTotalPaid() {
        return asset.getPurchases().stream()
                .map(p -> {
                    BigDecimal amount = new BigDecimal(p.getAmount());
                    BigDecimal unitPrice = new BigDecimal(p.getUnitPrice());
                    BigDecimal fees = new BigDecimal(p.getFees());
                    return amount.multiply(unitPrice).add(fees);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getTotalPaid() {
        if (isUnitPriceUnknown()) {
            return IGNORED;
        }
        return totalPaidDecimal
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    public String getPaidUnitPrice() {
        if (isUnitPriceUnknown()) {
            return IGNORED;
        }
        return totalPaidDecimal.divide(amountDecimal, 2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    public String getCurrentUnitPrice() {
        if (isUnitPriceUnknown()) {
            return UNKNOWN;
        }
        return currentUnitPriceInEuros
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    protected BigDecimal computeCurrentBalanceDecimal() {
        if (isUnitPriceUnknown()) {
            return null;
        }
        return currentUnitPriceInEuros.multiply(amountDecimal);
    }

    public String getCurrentBalance() {
        if (currentBalanceDecimal == null) {
            return UNKNOWN;
        }
        return currentBalanceDecimal
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    protected BigDecimal computeAbsoluteDeltaDecimal() {
        if (currentBalanceDecimal == null) {
            return null;
        }
        return currentBalanceDecimal.subtract(totalPaidDecimal);
    }

    public String getAbsoluteDelta() {
        if (absoluteDeltaDecimal == null) {
            return UNKNOWN;
        }
        BigDecimal absoluteDelta = absoluteDeltaDecimal
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return withPlusOrMinusPrefix(absoluteDelta);
    }

    private BigDecimal computePercentageChangeDecimal() {
        if (currentBalanceDecimal == null) {
            return null;
        }
        if (totalPaidDecimal.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return currentBalanceDecimal
                .subtract(totalPaidDecimal)
                .divide(totalPaidDecimal, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public String getPercentChange() {
        if (percentageChangeDecimal == null) {
            return NON_APPLICABLE;
        }
        BigDecimal percentageChange = percentageChangeDecimal
                .setScale(0, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return withPlusOrMinusPrefix(percentageChange) + "%";
    }

    protected BigDecimal getAbsoluteDeltaDecimal() {
        return absoluteDeltaDecimal;
    }

    protected BigDecimal getTotalPaidDecimal() {
        return totalPaidDecimal;
    }

    protected BigDecimal getCurrentBalanceDecimal() {
        return currentBalanceDecimal;
    }

    @Override
    public int compareTo(@NotNull ConsolidatedAsset object) {
        if (object.percentageChangeDecimal == null && this.percentageChangeDecimal == null) return 0;
        if (object.percentageChangeDecimal != null && this.percentageChangeDecimal == null) return 1;
        if (object.percentageChangeDecimal == null) return -1;
        return this.percentageChangeDecimal.compareTo(object.percentageChangeDecimal);
    }
}
