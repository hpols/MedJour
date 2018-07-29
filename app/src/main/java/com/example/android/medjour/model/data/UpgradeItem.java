package com.example.android.medjour.model.data;

/**
 * Used for storing the (hard coded) info about the item we're selling.
 * <p>
 * This POJO class is used only for example purposes - you don't need need it in your code.
 */
public class UpgradeItem {
    private final String name;

    // Micros are used for prices to avoid rounding errors when converting between currencies.
    private final long priceMicros;

    public UpgradeItem(String name, long price) {
        this.name = name;
        this.priceMicros = price;
    }

    public String getName() {
        return name;
    }

    public long getPriceMicros() {
        return priceMicros;
    }
}
