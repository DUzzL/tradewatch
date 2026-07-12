package dev.duzzl.tradewatch.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.duzzl.tradewatch.TradeWatchClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;
import net.minecraft.world.item.enchantment.Enchantment;
import dev.duzzl.tradewatch.util.EnchantmentRegistry;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Map;

public final class TradeWatchConfigManager {
    private static final org.slf4j.Logger LOGGER = TradeWatchClient.LOGGER;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("tradewatch.json");
    private static TradeWatchConfig config = new TradeWatchConfig();
    private static Registry<Enchantment> lastValidatedRegistry;
    private TradeWatchConfigManager() { }
    public static TradeWatchConfig get() { return config; }
    public static void load() {
        if (!Files.exists(PATH)) return;
        try { config = GSON.fromJson(Files.readString(PATH), TradeWatchConfig.class); if (config == null || config.watchedEnchantments == null) config = new TradeWatchConfig(); sanitize(); }
        catch (IOException | JsonParseException exception) { TradeWatchClient.LOGGER.warn("Could not read TradeWatch configuration; starting with an empty wishlist.", exception); config = new TradeWatchConfig(); }
    }
    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Path temporary = PATH.resolveSibling("tradewatch.json.tmp");
            Files.writeString(temporary, GSON.toJson(config), StandardCharsets.UTF_8);
            try { Files.move(temporary, PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ignored) { Files.move(temporary, PATH, StandardCopyOption.REPLACE_EXISTING); }
        }
        catch (IOException exception) { TradeWatchClient.LOGGER.warn("Could not save TradeWatch configuration.", exception); }
    }
    public static void watch(String id, int level, int price) { config.watchedEnchantments.computeIfAbsent(id, ignored -> new java.util.LinkedHashMap<>()).put(Integer.toString(level), new TradeWatchConfig.PriceEntry(price)); save(); }
    public static void removeEnchantment(String id) { config.watchedEnchantments.remove(id); save(); }
    public static void removeLevel(String id, int level) { Map<String, TradeWatchConfig.PriceEntry> levels = config.watchedEnchantments.get(id); if (levels != null) { levels.remove(Integer.toString(level)); if (levels.isEmpty()) config.watchedEnchantments.remove(id); } save(); }
    public static int priceFor(String id, int level) { Map<String, TradeWatchConfig.PriceEntry> levels = config.watchedEnchantments.get(id); TradeWatchConfig.PriceEntry entry = levels == null ? null : levels.get(Integer.toString(level)); return entry == null ? -1 : entry.maxPrice; }
    /** Called after registry synchronization; removes configuration made invalid by datapacks or malformed files. */
    public static void validateRuntimeEntries() {
        EnchantmentRegistry.get().ifPresent(registry -> {
            if (registry == lastValidatedRegistry) return;
            lastValidatedRegistry = registry;
            boolean changed = false;
            Iterator<Map.Entry<String, Map<String, TradeWatchConfig.PriceEntry>>> iterator = config.watchedEnchantments.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Map<String, TradeWatchConfig.PriceEntry>> entry = iterator.next();
                var enchantment = registry.getOptional(Identifier.tryParse(entry.getKey()));
                if (enchantment.isEmpty()) { LOGGER.warn("Ignoring unknown TradeWatch enchantment {}.", entry.getKey()); iterator.remove(); changed = true; continue; }
                int maximum = enchantment.get().getMaxLevel();
                if (entry.getValue().entrySet().removeIf(level -> Integer.parseInt(level.getKey()) > maximum)) changed = true;
                if (entry.getValue().isEmpty()) { iterator.remove(); changed = true; }
            }
            if (changed) save();
        });
    }
    private static void sanitize() {
        Iterator<Map.Entry<String, Map<String, TradeWatchConfig.PriceEntry>>> entries = config.watchedEnchantments.entrySet().iterator();
        while (entries.hasNext()) { Map.Entry<String, Map<String, TradeWatchConfig.PriceEntry>> entry = entries.next(); if (Identifier.tryParse(entry.getKey()) == null || entry.getValue() == null) { entries.remove(); continue; } entry.getValue().entrySet().removeIf(level -> { try { int value = Integer.parseInt(level.getKey()); return value < 1 || level.getValue() == null || level.getValue().maxPrice < 1 || level.getValue().maxPrice > 64; } catch (NumberFormatException ignored) { return true; } }); if (entry.getValue().isEmpty()) entries.remove(); }
    }
}
