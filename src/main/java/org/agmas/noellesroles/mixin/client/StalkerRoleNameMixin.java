package org.agmas.noellesroles.mixin.client;

import org.agmas.noellesroles.component.AdmirerPlayerComponent;
import org.agmas.noellesroles.component.StalkerPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.gui.RoleNameRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 跟踪者和爱慕者角色名称显示 Mixin
 * 
 * 功能：
 * 1. 在跟踪者名称后显示阶段信息（一阶/二阶/三阶）
 * 2. 在爱慕者名称后显示能量信息
 */
@Mixin(RoleNameRenderer.class)
public class StalkerRoleNameMixin {
    
    @Shadow
    private static float nametagAlpha;
    
    /**
     * 在角色名称渲染后添加阶段/能量信息
     */
    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I", ordinal = 0, shift = At.Shift.AFTER))
    private static void renderPhaseInfo(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.player.getWorld());
        
        // 只有旁观者或创作模式能看到详细信息
        if (!TMMClient.isPlayerSpectatingOrCreative()) return;
        
        // 获取当前查看的玩家（通过射线追踪或其他方式）
        // 这里使用当前玩家本身来检查角色
        PlayerEntity localPlayer = client.player;
        
        // =============== 跟踪者阶段显示 ===============
        if (gameWorld.isRole(localPlayer, ModRoles.STALKER)) {
            StalkerPlayerComponent stalkerComp = StalkerPlayerComponent.KEY.get(localPlayer);
            if (stalkerComp.isStalkerMarked && stalkerComp.phase > 0) {
                Text phaseText = getPhaseText(stalkerComp.phase);
                // 在角色名称下方显示阶段信息
                context.drawTextWithShadow(renderer, phaseText, -renderer.getWidth(phaseText) / 2, 12, 
                    ModRoles.STALKER.color() | (int) (nametagAlpha * 255.0F) << 24);
            }
        }
        
        // =============== 爱慕者能量显示 ===============
        if (gameWorld.isRole(localPlayer, ModRoles.ADMIRER)) {
            AdmirerPlayerComponent admirerComp = AdmirerPlayerComponent.KEY.get(localPlayer);
            if (admirerComp.isAdmirerMarked && !admirerComp.hasTransformed) {
                Text energyText = Text.literal(String.format("[%d/%d]", admirerComp.energy, AdmirerPlayerComponent.MAX_ENERGY))
                    .formatted(Formatting.LIGHT_PURPLE);
                // 在角色名称下方显示能量信息
                context.drawTextWithShadow(renderer, energyText, -renderer.getWidth(energyText) / 2, 12,
                    ModRoles.ADMIRER.color() | (int) (nametagAlpha * 255.0F) << 24);
            }
        }
    }
    
    /**
     * 获取阶段文本
     */
    private static Text getPhaseText(int phase) {
        return switch (phase) {
            case 1 -> Text.translatable("hud.noellesroles.stalker.phase1_short").formatted(Formatting.DARK_PURPLE);
            case 2 -> Text.translatable("hud.noellesroles.stalker.phase2_short").formatted(Formatting.RED);
            case 3 -> Text.translatable("hud.noellesroles.stalker.phase3_short").formatted(Formatting.DARK_RED);
            default -> Text.empty();
        };
    }
}