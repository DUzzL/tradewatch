package dev.duzzl.tradewatch.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Optional;

/** Accesses the synced dynamic registry, never a hard-coded enchantment list. */
public final class EnchantmentRegistry {
    private EnchantmentRegistry() { }
    public static Optional<Registry<Enchantment>> get() {
        Minecraft client = Minecraft.getInstance();
        return client.level == null ? Optional.empty() : Optional.of(client.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT));
    }
    public static Optional<Enchantment> find(String raw) {
        Identifier id = Identifier.tryParse(raw);
        return id == null ? Optional.empty() : get().flatMap(registry -> registry.getOptional(id));
    }
}
