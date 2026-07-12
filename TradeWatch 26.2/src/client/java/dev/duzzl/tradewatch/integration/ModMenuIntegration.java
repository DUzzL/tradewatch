package dev.duzzl.tradewatch.integration;

import dev.duzzl.tradewatch.gui.TradeWatchScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
public final class ModMenuIntegration implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() { return TradeWatchScreen::new; }
}
