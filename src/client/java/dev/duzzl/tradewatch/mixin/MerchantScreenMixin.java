package dev.duzzl.tradewatch.mixin;

import dev.duzzl.tradewatch.trade.MerchantScreenState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantScreen.class)
abstract class MerchantScreenMixin {
    @Shadow private int scrollOff;
    @Inject(method = "extractContents", at = @At("HEAD"))
    private void tradewatch$highlight(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
        MerchantScreen screen = (MerchantScreen) (Object) this;
        int left = (screen.width - 276) / 2, top = (screen.height - 166) / 2;
        for (int row = 0; row < 7; row++) if (MerchantScreenState.isMatch(scrollOff + row)) graphics.fill(left + 4, top + 17 + row * 20, left + 93, top + 36 + row * 20, 0x6040FF40);
    }
    @Inject(method = "extractContents", at = @At("TAIL"))
    private void tradewatch$overlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
        MerchantScreen screen = (MerchantScreen) (Object) this;
        MerchantScreenState.extractOverlay(graphics, screen.width, screen.height);
    }
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void tradewatch$clickOverlay(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        MerchantScreen screen = (MerchantScreen) (Object) this;
        if (MerchantScreenState.click(event.x(), event.y(), screen.width, screen.height)) cir.setReturnValue(true);
    }
}
