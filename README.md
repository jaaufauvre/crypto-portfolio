# crypto-portfolio

Fetch prices for a list of crypto assets and generate a simple portfolio summary. See also: https://www.coinapi.io.

[![](https://img.shields.io/badge/license-The%20Unlicense-yellow.svg)](https://github.com/jaaufauvre/crypto-portfolio/blob/main/LICENSE)

## Usage

1. Get a [CoinAPI](https://www.coinapi.io/) key
2. Run the program:

```console
mvn compile exec:java -Dexec.mainClass=org.crypto_portfolio.Main \
                      -Dcrypto_portfolio.coinapi.key={xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx} \
                      -Dcrypto_portfolio.portfolio.path=src/main/resources/portfolio.json \
                      -Dcrypto_portfolio.date-filter.not-before={yyyy-mm-dd} \
                      -Dcrypto_portfolio.ignore-errors=false
```

3. Default output:

```console
| Asset     | Amount | @ (Paid) | @ (Current) | Paid        | Balance      | Gain/Loss     | Change     |
| --------- | ------ | -------- | ----------- | ----------- | ------------ | ------------- | ---------- |
| AVAX      | 100    | 3.81     | 2.36        | 381         | 236          | -145          | -38%       |
| DOT       | 100    | 6.71     | 6.65        | 671.25      | 665          | -6.25         | -1%        |
| ETH       | 101    | 14.06    | 610.65      | 1419.71     | 61675.65     | +60255.94     | +4244%     |
| **TOTAL** | -      | -        | -           | **2471.96** | **62576.65** | **+60104.69** | **+2431%** |
```

## Portfolio JSON

Update [`portfolio.json`](./src/main/resources/portfolio.json). Format:

```javascript
[
  {
    "id": "{symbol}",
    "name": "{name}",
    "purchases": [
      {
        "comment": "{optional}",
        "amount": "{currency/token amount}",
        "@": "{unit price}",
        "fees": "{transaction fees}",
        "currency": "EUR",
        "date": "{purchase date, format: yyyy-mm-dd, optional}",
        "exchange": "{exchange name or url, optional}",
        "location": "{blockchain address or url, optional}"
      }
    ]
  }
]
```

## Troubleshooting

> _An error happened: You didn't specify API key or it is incorrectly formatted. You should do it in query string parameter `apikey` or in http header named `X-CoinAPI-Key`_

Double check your API key value (`-Dcrypto_portfolio.coinapi.key`).

> _Enter the missing ETH/EUR conversion rate:_

The conversion rate was not found, you have the possibility to type a value and press "Enter".

## See Also
https://docs.coinapi.io/
