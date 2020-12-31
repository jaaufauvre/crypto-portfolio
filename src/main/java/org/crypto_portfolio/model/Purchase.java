package org.crypto_portfolio.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Purchase {
    public String comment;
    public String amount;
    @JsonProperty("@")
    public String unitPrice;
    public String fees;
    public String currency;
    public String date;
    public String exchange;
    public String location;
}
