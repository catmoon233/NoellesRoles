package org.agmas.noellesroles.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 统一金币显示 HUD Mixin
 * 在右上角显示所有需要金币的职业的余额
 */
@Mixin(Gui.class)
public class MoneyHudMixin {
    
    @Inject(method = "renderEffects", at = @At("RETURN"))
    private void renderMoneyDisplay(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
//        MinecraftClient client = MinecraftClient.getInstance();
//        if (client.player == null || client.world == null) return;
//
//        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.world);
//
//        // 检查是否是需要显示金币的职业
//        boolean isPostman = gameWorld.isRole(client.player, ModRoles.POSTMAN);
//        boolean isDetective = gameWorld.isRole(client.player, ModRoles.DETECTIVE);
//        boolean isEngineer = gameWorld.isRole(client.player, ModRoles.ENGINEER);
//        boolean isSlipperyGhost = gameWorld.isRole(client.player, ModRoles.SLIPPERY_GHOST);
//        boolean isConspirator = gameWorld.isRole(client.player, ModRoles.CONSPIRATOR);
//        boolean isBoxer = gameWorld.isRole(client.player, ModRoles.BOXER);
//        boolean isAvenger = gameWorld.isRole(client.player, ModRoles.AVENGER);
//        boolean isTelegrapher = gameWorld.isRole(client.player, ModRoles.TELEGRAPHER);
//        boolean isPuppeteer = gameWorld.isRole(client.player, ModRoles.PUPPETEER);
//
//        // 如果不是任何需要显示金币的职业，返回
//        if (!isPostman && !isDetective && !isEngineer && !isSlipperyGhost &&
//            !isConspirator && !isBoxer && !isAvenger && !isTelegrapher && !isPuppeteer) return;
//
//        if (!GameFunctions.isPlayerAliveAndSurvival(client.player)) return;
//
//        PlayerShopComponent shopComponent = PlayerShopComponent.KEY.get(client.player);
//        int screenWidth = client.getWindow().getScaledWidth();
//        TextRenderer textRenderer = client.textRenderer;
//
//        // 通用的金币显示文本
//        Text moneyText = Text.translatable("tip.noellesroles.money.balance", shopComponent.balance)
//            .formatted(Formatting.GOLD);
//
//        int textWidth = textRenderer.getWidth(moneyText);
//        int x = screenWidth - textWidth - 10;  // 距离右边缘10像素
//        int y = 10;  // 距离顶部10像素
//
//        context.drawTextWithShadow(textRenderer, moneyText, x, y, 0xFFAA00);
    }
}