package org.crypto_portfolio.model;

import lombok.Data;

import java.util.List;

@Data
public class Asset {
    public String id;
    public String name;
    List<Purchase> purchases;
}
