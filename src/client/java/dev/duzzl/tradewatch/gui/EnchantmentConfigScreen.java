package dev.duzzl.tradewatch.gui;

import dev.duzzl.tradewatch.config.TradeWatchConfigManager;
import dev.duzzl.tradewatch.util.EnchantmentRegistry;
import dev.duzzl.tradewatch.util.RomanNumerals;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

public final class EnchantmentConfigScreen extends Screen {
    private final Screen parent; private final Enchantment enchantment; private final String id;
    public EnchantmentConfigScreen(Screen parent, Enchantment enchantment) { super(enchantment.description()); this.parent = parent; this.enchantment = enchantment; this.id = EnchantmentRegistry.get().orElseThrow().getKey(enchantment).toString(); }
    @Override protected void init() {
        int left = width / 2 - 150;
        for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
            int y = 38 + (level - 1) * 25; final int value = level; int current = TradeWatchConfigManager.priceFor(id, level); boolean enabled = current >= 1;
            PriceSlider slider = addRenderableWidget(new PriceSlider(left + 126, y, 174, enabled ? current : 64, value)); slider.active = enabled;
            addRenderableWidget(Button.builder(Component.literal(enabled ? "Enabled" : "Disabled"), button -> { boolean nowEnabled = TradeWatchConfigManager.priceFor(id, value) < 1; if (nowEnabled) TradeWatchConfigManager.watch(id, value, slider.price()); else TradeWatchConfigManager.removeLevel(id, value); button.setMessage(Component.literal(nowEnabled ? "Enabled" : "Disabled")); slider.active = nowEnabled; }).bounds(left + 28, y, 92, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Back"), button -> onClose()).bounds(width / 2 - 50, height - 28, 100, 20).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) { extractMenuBackground(graphics); graphics.centeredText(font, title, width / 2, 8, 0xFFFFFFFF); int left = width / 2 - 150; for (int level = 1; level <= enchantment.getMaxLevel(); level++) graphics.centeredText(font, RomanNumerals.of(level), left + 13, 44 + (level - 1) * 25, 0xFFFFFFFF); super.extractRenderState(graphics, mouseX, mouseY, partialTick); }
    @Override public void onClose() { TradeWatchConfigManager.save(); minecraft.setScreenAndShow(parent); }
    private final class PriceSlider extends AbstractSliderButton {
        private final int level;
        PriceSlider(int x, int y, int width, int price, int level) { super(x, y, width, 20, Component.empty(), (price - 1) / 63.0); this.level = level; updateMessage(); }
        int price() { return 1 + (int) Math.round(value * 63); }
        @Override protected void updateMessage() { setMessage(Component.literal(price() + " Emeralds")); }
        @Override protected void applyValue() { TradeWatchConfigManager.watch(id, level, price()); }
    }
}
