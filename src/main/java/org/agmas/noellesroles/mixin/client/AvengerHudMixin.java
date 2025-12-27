package org.agmas.noellesroles.mixin.client;

import org.agmas.noellesroles.component.AvengerPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.gui.RoleNameRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 复仇者 HUD Mixin
 * 在屏幕上显示绑定目标和激活状态
 */
@Mixin(RoleNameRenderer.class)
public abstract class AvengerHudMixin {

    @Inject(method = "renderHud", at = @At("HEAD"))
    private static void avengerHudRenderer(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.player.getWorld());
        
        // 只有复仇者角色才显示 HUD
        if (!gameWorld.isRole(client.player, ModRoles.AVENGER)) return;
        if (!TMMClient.isPlayerAliveAndInSurvival()) return;
        
        AvengerPlayerComponent avengerComponent = AvengerPlayerComponent.KEY.get(client.player);
        
        context.getMatrices().push();
        
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int yOffset = screenHeight - 28;  // 右下角
        int xOffset = screenWidth - 180;  // 距离右边缘
        
        if (avengerComponent.activated) {
            // 复仇已激活 - 显示凶手信息
            Text statusText = Text.translatable("tip.noellesroles.avenger.activated", 
                avengerComponent.getKillerName().isEmpty() ? "???" : avengerComponent.getKillerName())
                .formatted(Formatting.RED, Formatting.BOLD);
            
            context.drawTextWithShadow(renderer, statusText, xOffset, yOffset, Colors.RED);
            
            // 如果知道凶手，显示凶手头像
            if (avengerComponent.killerUuid != null &&
                client.player.networkHandler.getPlayerListEntry(avengerComponent.killerUuid) != null) {
                PlayerSkinDrawer.draw(context,
                    client.player.networkHandler.getPlayerListEntry(avengerComponent.killerUuid).getSkinTextures().texture(),
                    xOffset, yOffset - 14, 12);
                
                Text killerName = Text.literal(avengerComponent.getKillerName()).formatted(Formatting.RED);
                context.drawTextWithShadow(renderer, killerName, xOffset + 16, yOffset - 12, Colors.RED);
            }
        } else if (avengerComponent.bound && avengerComponent.targetPlayer != null) {
            // 已绑定目标 - 显示保护目标
            if (client.player.networkHandler.getPlayerListEntry(avengerComponent.targetPlayer) != null) {
                // 显示目标头像
                PlayerSkinDrawer.draw(context,
                    client.player.networkHandler.getPlayerListEntry(avengerComponent.targetPlayer).getSkinTextures().texture(),
                    xOffset, yOffset, 12);
                
                Text targetText = Text.translatable("tip.noellesroles.avenger.target",
                    avengerComponent.targetName).formatted(Formatting.GOLD);
                context.drawTextWithShadow(renderer, targetText, xOffset + 16, yOffset + 2, 0xFFAA00);
            }
        } else {
            // 等待绑定目标
            Text waitingText = Text.translatable("tip.noellesroles.avenger.waiting")
                .formatted(Formatting.GRAY);
            context.drawTextWithShadow(renderer, waitingText, xOffset, yOffset, Colors.GRAY);
        }
        
        context.getMatrices().pop();
    }
}