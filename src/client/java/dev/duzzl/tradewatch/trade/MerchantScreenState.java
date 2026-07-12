package dev.duzzl.tradewatch.trade;

import dev.duzzl.tradewatch.config.TradeWatchConfigManager;
import dev.duzzl.tradewatch.util.RomanNumerals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Per-open-screen cache: offers are inspected once after their server data arrives. */
public final class MerchantScreenState {
    private static final int PANEL_WIDTH = 128;
    private static final int PANEL_HEIGHT = 36;
    private static MerchantScreen activeScreen;
    private static boolean notified;
    private static int offerSignature;
    private static final List<TradeMatch> matches = new ArrayList<>();
    private static final Set<Integer> matchingOfferIndices = new HashSet<>();
    private static String summaryText = "";
    private static String stopText = "";
    private MerchantScreenState() { }
    public static void tick(Minecraft client) {
        if (!(client.gui.screen() instanceof MerchantScreen screen)) { reset(); return; }
        if (screen != activeScreen) { activeScreen = screen; matches.clear(); matchingOfferIndices.clear(); notified = false; offerSignature = Integer.MIN_VALUE; }
        if (screen.getMenu().getOffers().isEmpty()) return;
        int currentSignature = signature(screen);
        if (currentSignature == offerSignature) return;
        offerSignature = currentSignature;
        matches.clear();
        matches.addAll(TradeMatcher.findMatches(screen.getMenu().getOffers()));
        rebuildDisplayCache();
        if (!notified && !matches.isEmpty()) { notified = true; if (client.player != null) client.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.65f, 1.25f); }
    }
    public static boolean isMatch(int index) { return matchingOfferIndices.contains(index); }
    public static void extractOverlay(GuiGraphicsExtractor graphics, int width, int height) {
        if (matches.isEmpty()) return;
        int x = panelX(width), y = panelY(height);
        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xD0204020);
        graphics.outline(x, y, PANEL_WIDTH, PANEL_HEIGHT, 0xFF55FF55);
        graphics.centeredText(Minecraft.getInstance().font, "TRADE FOUND!", x + PANEL_WIDTH / 2, y + 3, 0xFFFFFF55);
        graphics.centeredText(Minecraft.getInstance().font, summaryText, x + PANEL_WIDTH / 2, y + 14, 0xFFFFFFFF);
        graphics.centeredText(Minecraft.getInstance().font, stopText, x + PANEL_WIDTH / 2, y + 25, 0xFFFF8080);
    }
    public static boolean click(double mouseX, double mouseY, int width, int height) {
        if (matches.isEmpty()) return false; int x = panelX(width), y = panelY(height);
        if (mouseY < y + 21 || mouseY > y + PANEL_HEIGHT) return false;
        if (mouseX >= x && mouseX < x + PANEL_WIDTH) { String id = matches.getFirst().enchantmentId(); TradeWatchConfigManager.removeEnchantment(id); matches.removeIf(match -> match.enchantmentId().equals(id)); rebuildDisplayCache(); return true; }
        return false;
    }
    private static int signature(MerchantScreen screen) {
        int signature = 1;
        for (MerchantOffer offer : screen.getMenu().getOffers()) {
            signature = 31 * signature + offer.getCostA().getCount();
            signature = 31 * signature + offer.getCostB().getCount();
            signature = 31 * signature + offer.getSpecialPriceDiff();
            signature = 31 * signature + ItemStack.hashItemAndComponents(offer.getResult());
        }
        return signature;
    }
    private static void rebuildDisplayCache() {
        matchingOfferIndices.clear();
        for (TradeMatch match : matches) matchingOfferIndices.add(match.offerIndex());
        if (matches.isEmpty()) { summaryText = ""; stopText = ""; return; }
        TradeMatch first = matches.getFirst();
        summaryText = matches.size() == 1 ? first.enchantment().description().getString() + " " + RomanNumerals.of(first.level()) + " · " + first.emeraldPrice() : matches.size() + " matching trades";
        stopText = "Stop watching " + first.enchantment().description().getString();
    }
    private static void reset() {
        activeScreen = null;
        matches.clear();
        matchingOfferIndices.clear();
        summaryText = "";
        stopText = "";
        notified = false;
        offerSignature = Integer.MIN_VALUE;
    }
    private static int panelX(int width) { return (width - PANEL_WIDTH) / 2; }
    private static int panelY(int height) { int merchantTop = (height - 166) / 2; return Math.max(2, merchantTop - PANEL_HEIGHT - 3); }
}
