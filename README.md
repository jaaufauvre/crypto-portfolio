# crypto-portfolio

Fetch prices for a list of crypto assets and generate a simple portfolio summary. See also: https://www.coinapi.io.

## Usage

1. Get a CoinAPI key (https://www.coinapi.io/)
2. Add it to [`Main.java`](./src/main/java/org/example/Main.java)
2. Run the program:

```console
mvn compile exec:java "-Dexec.mainClass=org.example.Main"
```

3. Expected output:

```console
| Asset     | Amount | @ (Paid)  | @ (Current) | Paid        | Balance      | Gain/Loss     | Change     |
| --------- | ------ | --------- | ----------- | ----------- | ------------ | ------------- | ---------- |
| ETH       | 101    | 14.056535 | 610.650000  | 1419.71     | 61675.65     | +60255.94     | +4244%     |
| DOT       | 100    | 6.712500  | 6.650000    | 671.25      | 665.00       | -6.25         | -1%        |
| AVAX      | 100    | 3.810000  | 2.360000    | 381.00      | 236.00       | -145.00       | -38%       |
| **TOTAL** | -      | -         | -           | **2471.96** | **62576.65** | **+60104.69** | **+2431%** |
```

## Portfolio JSON

Update [`portfolio.json`](./src/main/resources/portfolio.json)