package dev.duzzl.tradewatch.trade;

import dev.duzzl.tradewatch.config.TradeWatchConfigManager;
import dev.duzzl.tradewatch.util.EnchantmentRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import java.util.ArrayList;
import java.util.List;

public final class TradeMatcher {
    private TradeMatcher() { }
    public static List<TradeMatch> findMatches(Iterable<MerchantOffer> offers) {
        List<TradeMatch> matches = new ArrayList<>(); int index = 0;
        Registry<Enchantment> registry = EnchantmentRegistry.get().orElse(null);
        if (registry == null) return matches;
        for (MerchantOffer offer : offers) {
            if (!offer.getResult().is(Items.ENCHANTED_BOOK)) { index++; continue; }
            ItemEnchantments enchantments = offer.getResult().get(DataComponents.STORED_ENCHANTMENTS);
            if (enchantments == null || enchantments.isEmpty()) { index++; continue; }
            int emeralds = offer.getCostA().is(Items.EMERALD) ? offer.getCostA().getCount() : (offer.getCostB().is(Items.EMERALD) ? offer.getCostB().getCount() : 0);
            for (Holder<Enchantment> holder : enchantments.keySet()) {
                int level = enchantments.getLevel(holder);
                String id = registry.getKey(holder.value()).toString();
                if (id != null && emeralds > 0 && TradeWatchConfigManager.priceFor(id, level) >= emeralds) matches.add(new TradeMatch(index, id, holder.value(), level, emeralds));
            }
            index++;
        }
        return matches;
    }
}
