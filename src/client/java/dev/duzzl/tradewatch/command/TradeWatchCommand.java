package dev.duzzl.tradewatch.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.duzzl.tradewatch.config.TradeWatchConfig;
import dev.duzzl.tradewatch.config.TradeWatchConfigManager;
import dev.duzzl.tradewatch.gui.TradeWatchScreen;
import dev.duzzl.tradewatch.util.EnchantmentRegistry;
import dev.duzzl.tradewatch.util.RomanNumerals;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;
import java.util.Map;

public final class TradeWatchCommand {
    private TradeWatchCommand() { }
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal("tradewatch");
        root.executes(context -> { Minecraft client = Minecraft.getInstance(); client.setScreenAndShow(new TradeWatchScreen(null)); return 1; });
        RequiredArgumentBuilder<FabricClientCommandSource, Identifier> enchantment = RequiredArgumentBuilder.<FabricClientCommandSource, Identifier>argument("enchantment", IdentifierArgument.id()).suggests((context, builder) -> { EnchantmentRegistry.get().ifPresent(registry -> registry.keySet().forEach(id -> builder.suggest(id.toString()))); return builder.buildFuture(); });
        RequiredArgumentBuilder<FabricClientCommandSource, Integer> level = RequiredArgumentBuilder.argument("level", IntegerArgumentType.integer(1));
        level.suggests((context, builder) -> { Identifier id = context.getArgument("enchantment", Identifier.class); EnchantmentRegistry.find(id.toString()).ifPresent(value -> { for (int candidate = 1; candidate <= value.getMaxLevel(); candidate++) builder.suggest(candidate); }); return builder.buildFuture(); });
        RequiredArgumentBuilder<FabricClientCommandSource, String> price = RequiredArgumentBuilder.argument("price", PriceArgumentType.price());
        price.executes(context -> add(context.getSource(), context.getArgument("enchantment", Identifier.class).toString(), IntegerArgumentType.getInteger(context, "level"), StringArgumentType.getString(context, "price")));
        level.then(price);
        enchantment.then(level);
        root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("add").then(enchantment));
        RequiredArgumentBuilder<FabricClientCommandSource, Identifier> removal = RequiredArgumentBuilder.<FabricClientCommandSource, Identifier>argument("enchantment", IdentifierArgument.id()).suggests((context, builder) -> { EnchantmentRegistry.get().ifPresent(registry -> registry.keySet().forEach(id -> builder.suggest(id.toString()))); return builder.buildFuture(); });
        removal.executes(context -> remove(context.getSource(), context.getArgument("enchantment", Identifier.class).toString(), 0));
        removal.then(RequiredArgumentBuilder.<FabricClientCommandSource, Integer>argument("level", IntegerArgumentType.integer(1)).executes(context -> remove(context.getSource(), context.getArgument("enchantment", Identifier.class).toString(), IntegerArgumentType.getInteger(context, "level"))));
        root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("remove").then(removal));
        root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("list").executes(context -> list(context.getSource())));
        root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("clear").executes(context -> { context.getSource().sendFeedback(Component.literal("Run /tradewatch clear confirm to clear the entire TradeWatch wishlist.")); return 1; }).then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("confirm").executes(context -> { TradeWatchConfigManager.get().watchedEnchantments.clear(); TradeWatchConfigManager.save(); context.getSource().sendFeedback(Component.literal("TradeWatch wishlist cleared.")); return 1; })));
        dispatcher.register(root);
    }
    private static Enchantment resolve(FabricClientCommandSource source, String id) { Enchantment result = EnchantmentRegistry.find(id).orElse(null); if (result == null) source.sendError(Component.literal("Unknown enchantment: " + id)); return result; }
    private static int add(FabricClientCommandSource source, String id, int level, String rawPrice) { Enchantment enchantment = resolve(source, id); if (enchantment == null) return 0; if (level > enchantment.getMaxLevel()) { source.sendError(Component.literal(enchantment.description().getString() + " only supports levels I-" + RomanNumerals.of(enchantment.getMaxLevel()) + ".")); return 0; } if (!rawPrice.startsWith("price:")) { source.sendError(Component.literal("Use price:<1-64>, for example price:32.")); return 0; } try { int price = Integer.parseInt(rawPrice.substring(6)); if (price < 1 || price > 64) throw new NumberFormatException(); TradeWatchConfigManager.watch(id, level, price); source.sendFeedback(Component.literal("Now watching " + enchantment.description().getString() + " " + RomanNumerals.of(level) + " for " + price + " Emeralds or less.")); return 1; } catch (NumberFormatException exception) { source.sendError(Component.literal("Price must be between 1 and 64.")); return 0; } }
    private static int remove(FabricClientCommandSource source, String id, int level) { Enchantment enchantment = resolve(source, id); if (enchantment == null) return 0; if (level > enchantment.getMaxLevel()) { source.sendError(Component.literal(enchantment.description().getString() + " only supports levels I-" + RomanNumerals.of(enchantment.getMaxLevel()) + ".")); return 0; } if (level > 0) { TradeWatchConfigManager.removeLevel(id, level); source.sendFeedback(Component.literal("Stopped watching " + enchantment.description().getString() + " " + RomanNumerals.of(level) + ".")); } else { TradeWatchConfigManager.removeEnchantment(id); source.sendFeedback(Component.literal("Stopped watching " + enchantment.description().getString() + ".")); } return 1; }
    private static int list(FabricClientCommandSource source) { source.sendFeedback(Component.literal("TradeWatch Wishlist:")); if (TradeWatchConfigManager.get().watchedEnchantments.isEmpty()) source.sendFeedback(Component.literal("(empty)")); for (Map.Entry<String, Map<String, TradeWatchConfig.PriceEntry>> entry : TradeWatchConfigManager.get().watchedEnchantments.entrySet()) { Enchantment enchantment = EnchantmentRegistry.find(entry.getKey()).orElse(null); if (enchantment == null) continue; for (Map.Entry<String, TradeWatchConfig.PriceEntry> level : entry.getValue().entrySet()) source.sendFeedback(Component.literal(enchantment.description().getString() + " " + RomanNumerals.of(Integer.parseInt(level.getKey())) + " - " + level.getValue().maxPrice + " Emeralds")); } return 1; }
}
