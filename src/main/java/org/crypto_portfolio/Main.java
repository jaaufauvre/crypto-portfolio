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
import java.util.List;
import java.util.Scanner;

import static net.steppschuh.markdowngenerator.table.Table.*;

public class Main {

    private static final String API_KEY = "Paste your https://www.coinapi.io API key here";
    private static final OkHttpClient CLIENT = new OkHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        List<Asset> portfolio = readPortfolio();
        ConsolidatedPortfolio consolidatedPortfolio = new ConsolidatedPortfolio();
        for (Asset asset : portfolio) {
            consolidatedPortfolio.add(toConsolidatedAsset(asset));
        }
        display(consolidatedPortfolio);
    }

    private static List<Asset> readPortfolio() throws IOException {
        byte[] portfolioBytes = Files.readAllBytes(Paths.get("src/main/resources/portfolio.json"));
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
        } else {
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
                .sorted()
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
