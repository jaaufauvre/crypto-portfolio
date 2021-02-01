package org.crypto_portfolio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.steppschuh.markdowngenerator.text.emphasis.BoldText;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.crypto_portfolio.model.Asset;
import org.crypto_portfolio.model.CoinApiRate;
import org.crypto_portfolio.model.ConsolidatedAsset;
import org.crypto_portfolio.model.ConsolidatedPortfolio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.Comparator;

import static net.steppschuh.markdowngenerator.table.Table.*;

public class Main {
    private static final OkHttpClient CLIENT = new OkHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String API_KEY, PORTFOLIO_PATH;
    private static final LocalDate NOT_BEFORE;
    private static final Boolean IGNORE_ERRORS;
    static {
        String path = System.getProperty("crypto_portfolio.portfolio.path");
        String notBefore = System.getProperty("crypto_portfolio.date-filter.not-before");
        String ignoreErrors = System.getProperty("crypto_portfolio.ignore-errors");
        API_KEY = System.getProperty("crypto_portfolio.coinapi.key");
        PORTFOLIO_PATH = path == null ? "src/main/resources/portfolio.json" : path;
        NOT_BEFORE = notBefore == null ? LocalDate.MIN : LocalDate.parse(notBefore);
        IGNORE_ERRORS = "true".equalsIgnoreCase(ignoreErrors);
    }

    public static void main(String[] args) throws Exception {
        List<Asset> portfolio = readPortfolio();
        filterPurchases(portfolio);
        ConsolidatedPortfolio consolidatedPortfolio = portfolio.stream()
                .map(Main::toConsolidatedAsset)
                .collect(Collectors.toCollection(ConsolidatedPortfolio::new));
        display(consolidatedPortfolio);
    }

    private static void filterPurchases(List<Asset> portfolio) {
        portfolio.forEach(asset -> asset.getPurchases().removeIf(p -> p.date != null && LocalDate.parse(p.date).isBefore(NOT_BEFORE)));
        portfolio.removeIf(asset -> asset.getPurchases().isEmpty());
    }

    private static List<Asset> readPortfolio() throws IOException {
        byte[] portfolioBytes = Files.readAllBytes(Paths.get(PORTFOLIO_PATH));
        return MAPPER.readValue(portfolioBytes, new TypeReference<List<Asset>>(){});
    }

    private static ConsolidatedAsset toConsolidatedAsset(Asset asset) {
        if (asset.getPurchases().stream().anyMatch(p -> !"EUR".equals(p.currency))) {
            throw new IllegalStateException("Only EUR is supported!");
        }
        Request request = new Request.Builder()
                .url(String.format("https://rest.coinapi.io/v1/exchangerate/%s/EUR?apikey=%s", asset.getId(), API_KEY))
                .build();
        String error;
        CoinApiRate rate = null;
        try {
            Response response = CLIENT.newCall(request).execute();
            rate = MAPPER.readValue(response.body().bytes(), CoinApiRate.class);
            error = rate.getError();
        } catch (Exception e) {
            error = e.getMessage();
        }
        if (error == null) {
            return new ConsolidatedAsset(asset, rate.getRate());
        }

        if (IGNORE_ERRORS) {
            // Create an asset with an "Unknown" unit price in €
            return new ConsolidatedAsset(asset, null);
        } else {
            // Expect the missing conversion rate to be typed in the console
            System.out.printf("An error happened: %s%n", error);
            System.out.printf("Enter the missing %s/EUR conversion rate: ", asset.getId());
            return new ConsolidatedAsset(asset, new Scanner(System.in).nextLine());
        }
    }

    private static void display(ConsolidatedPortfolio consolidatedPortfolio) {
        Builder tableBuilder = new Builder()
                .withAlignments(ALIGN_LEFT, ALIGN_LEFT, ALIGN_LEFT, ALIGN_LEFT, ALIGN_LEFT, ALIGN_LEFT, ALIGN_LEFT)
                .addRow("Asset", "Amount", "@ (Paid)", "@ (Current)", "Paid", "Balance", "Gain/Loss", "Change");
        consolidatedPortfolio
                .stream()
                .sorted(Comparator.comparing(ConsolidatedAsset::getRawPercentChange))
                .forEach(a -> tableBuilder.addRow(a.getName(), a.getAmount(),
                                                            a.getPaidUnitPrice(), a.getCurrentUnitPrice(),
                                                            a.getTotalPaid(), a.getCurrentBalance(),
                                                            a.getAbsoluteDelta(), a.getPercentChange()));
        tableBuilder.addRow(new BoldText("TOTAL"), "-", "-", "-",
                            new BoldText(consolidatedPortfolio.getTotalPaid()),
                            new BoldText(consolidatedPortfolio.getTotalCurrentBalance()),
                            new BoldText(consolidatedPortfolio.getTotalAbsoluteDelta()),
                            new BoldText(consolidatedPortfolio.getTotalPercentageChange()));
        System.out.println(tableBuilder.build());
    }
}
