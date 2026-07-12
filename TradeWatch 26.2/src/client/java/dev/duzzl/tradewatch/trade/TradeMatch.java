package dev.duzzl.tradewatch.trade;

import net.minecraft.world.item.enchantment.Enchantment;

public record TradeMatch(int offerIndex, String enchantmentId, Enchantment enchantment, int level, int emeraldPrice) { }
