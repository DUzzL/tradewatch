package dev.duzzl.tradewatch;

import com.mojang.blaze3d.platform.InputConstants;
import dev.duzzl.tradewatch.command.TradeWatchCommand;
import dev.duzzl.tradewatch.config.TradeWatchConfigManager;
import dev.duzzl.tradewatch.gui.TradeWatchScreen;
import dev.duzzl.tradewatch.trade.MerchantScreenState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TradeWatchClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("tradewatch");
    private static KeyMapping openKey;
    @Override public void onInitializeClient() {
        TradeWatchConfigManager.load();
        KeyMapping.Category category = KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("tradewatch", "general"));
        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.tradewatch.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, category));
        ClientTickEvents.END_CLIENT_TICK.register(client -> { while (openKey.consumeClick()) Minecraft.getInstance().setScreenAndShow(new TradeWatchScreen(client.gui.screen())); TradeWatchConfigManager.validateRuntimeEntries(); MerchantScreenState.tick(client); });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> TradeWatchCommand.register(dispatcher));
    }
}
