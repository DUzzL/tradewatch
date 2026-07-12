package dev.duzzl.tradewatch.gui;

import dev.duzzl.tradewatch.config.TradeWatchConfigManager;
import dev.duzzl.tradewatch.util.EnchantmentRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TradeWatchScreen extends Screen {
    private static final int LIST_TOP = 56;
    private static final int ROW_HEIGHT = 22;
    private final Screen parent;
    private final List<Enchantment> filtered = new ArrayList<>();
    private final List<Button> rows = new ArrayList<>();
    private EditBox search;
    private int scroll;
    private String query = "";

    public TradeWatchScreen(Screen parent) { super(Component.literal("TradeWatch")); this.parent = parent; }
    @Override protected void init() {
        rows.clear();
        search = addRenderableWidget(new EditBox(font, width / 2 - 150, 28, 300, 20, Component.literal("Search enchantments")));
        search.setHint(Component.literal("Search enchantments or registry ID"));
        search.setValue(query);
        search.setResponder(value -> { query = value; scroll = 0; updateRows(); });
        for (int row = 0; row < visibleRows(); row++) { final int rowIndex = row; rows.add(addRenderableWidget(Button.builder(Component.empty(), button -> openRow(rowIndex)).bounds(width / 2 - 150, LIST_TOP + row * ROW_HEIGHT, 286, 20).build())); }
        updateRows();
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose()).bounds(width / 2 - 50, height - 30, 100, 20).build());
    }
    private void refresh() {
        filtered.clear(); String needle = query.toLowerCase(java.util.Locale.ROOT);
        EnchantmentRegistry.get().ifPresent(registry -> registry.stream().filter(enchantment -> {
            String id = registry.getKey(enchantment).toString();
            return needle.isBlank() || id.contains(needle) || enchantment.description().getString().toLowerCase(java.util.Locale.ROOT).contains(needle);
        }).sorted(Comparator.comparing(e -> e.description().getString())).forEach(filtered::add));
    }
    private void openRow(int row) { int index = scroll + row; if (index < filtered.size()) minecraft.setScreenAndShow(new EnchantmentConfigScreen(this, filtered.get(index))); }
    private void updateRows() {
        refresh(); setScroll(scroll);
        for (int row = 0; row < rows.size(); row++) {
            int index = scroll + row; Button button = rows.get(row); boolean visible = index < filtered.size(); button.visible = visible; button.active = visible;
            if (visible) { Enchantment enchantment = filtered.get(index); String id = EnchantmentRegistry.get().orElseThrow().getKey(enchantment).toString(); String watched = TradeWatchConfigManager.get().watchedEnchantments.containsKey(id) ? "  [watched]" : ""; button.setMessage(Component.literal(enchantment.description().getString() + watched + "  ▼")); }
        }
    }
    @Override protected void rebuildWidgets() { super.rebuildWidgets(); }
    private int listBottom() { return height - 54; }
    private int visibleRows() { return Math.max(1, (listBottom() - LIST_TOP) / ROW_HEIGHT); }
    private int maxScroll() { return Math.max(0, filtered.size() - visibleRows()); }
    private void setScroll(int value) { scroll = Math.max(0, Math.min(maxScroll(), value)); }
    private int thumbHeight() { return filtered.size() <= visibleRows() ? listBottom() - LIST_TOP : Math.max(16, (listBottom() - LIST_TOP) * visibleRows() / filtered.size()); }
    private int thumbY() { int track = listBottom() - LIST_TOP - thumbHeight(); return LIST_TOP + (maxScroll() == 0 ? 0 : track * scroll / maxScroll()); }
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) { if (mouseX >= width / 2 - 150 && mouseX <= width / 2 + 150 && mouseY >= LIST_TOP && mouseY <= listBottom()) { setScroll(scroll - (int) Math.signum(verticalAmount)); updateRows(); return true; } return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount); }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) { int barX = width / 2 + 140; if (event.x() >= barX && event.x() <= barX + 10 && event.y() >= LIST_TOP && event.y() <= listBottom() && maxScroll() > 0) { double ratio = (event.y() - LIST_TOP - thumbHeight() / 2.0) / Math.max(1, listBottom() - LIST_TOP - thumbHeight()); setScroll((int) Math.round(ratio * maxScroll())); updateRows(); return true; } return super.mouseClicked(event, doubleClick); }
    @Override public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) { int barX = width / 2 + 140; if (event.x() >= barX && event.x() <= barX + 10 && event.y() >= LIST_TOP && event.y() <= listBottom() && maxScroll() > 0) { double ratio = (event.y() - LIST_TOP - thumbHeight() / 2.0) / Math.max(1, listBottom() - LIST_TOP - thumbHeight()); setScroll((int) Math.round(ratio * maxScroll())); updateRows(); return true; } return super.mouseDragged(event, dragX, dragY); }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractMenuBackground(graphics); graphics.centeredText(font, title, width / 2, 8, 0xFFFFFFFF);
        if (EnchantmentRegistry.get().isEmpty()) graphics.centeredText(font, "Join a world or server to load the enchantment registry.", width / 2, LIST_TOP + 8, 0xFFAAAAAA);
        int barX = width / 2 + 140; graphics.fill(barX, LIST_TOP, barX + 10, listBottom(), 0xFF303030); graphics.fill(barX, thumbY(), barX + 10, thumbY() + thumbHeight(), 0xFFB0B0B0);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }
    @Override public void onClose() { TradeWatchConfigManager.save(); minecraft.setScreenAndShow(parent); }
}
