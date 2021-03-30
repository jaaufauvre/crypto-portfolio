package org.crypto_portfolio.model;

import lombok.Data;

@Data
public class CoinApiRate {
    String asset_id_base;
    String asset_id_quote;
    String rate;
    String error;
}
