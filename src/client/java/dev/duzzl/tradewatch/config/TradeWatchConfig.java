package dev.duzzl.tradewatch.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TradeWatchConfig {
    public Map<String, Map<String, PriceEntry>> watchedEnchantments = new LinkedHashMap<>();
    public static final class PriceEntry {
        public int maxPrice;
        public PriceEntry() { }
        public PriceEntry(int maxPrice) { this.maxPrice = maxPrice; }
    }
}
