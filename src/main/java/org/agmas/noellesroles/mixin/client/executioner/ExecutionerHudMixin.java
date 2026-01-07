package org.agmas.noellesroles.mixin.client.executioner;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.gui.RoleNameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.executioner.ExecutionerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RoleNameRenderer.class)
public abstract class ExecutionerHudMixin {

    @Shadow private static float nametagAlpha;

    @Shadow private static Component nametag;


    @Inject(method = "renderHud", at = @At("HEAD"))
    private static void executionerHudRenderer(Font renderer, LocalPlayer player, GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.level());

        // 检查是否是Executioner角色且存活
        if (Minecraft.getInstance().player != null && gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.EXECUTIONER) && TMMClient.isPlayerAliveAndInSurvival()) {
            // 检查是否已经转变为杀手
            if (!gameWorldComponent.getRole(Minecraft.getInstance().player).canUseKiller()) {
                ExecutionerPlayerComponent executionerPlayerComponent = ExecutionerPlayerComponent.KEY.get(player);

                // 检查是否有选定的目标
                if (executionerPlayerComponent.target != null && executionerPlayerComponent.targetSelected) {
                    var playerListEntry = Minecraft.getInstance().player.connection.getPlayerInfo(executionerPlayerComponent.target);
                    if (playerListEntry == null) return;

                    context.pose().pushPose();
                    context.pose().translate((float) context.guiWidth() / 2.0F, (float) context.guiHeight() / 2.0F + 6.0F, 0.0F);
                    context.pose().scale(0.6F, 0.6F, 1.0F);

                    // 显示目标玩家名称
                    Component name = Component.literal("目标: " + playerListEntry.getProfile().getName());
                    context.drawString(renderer, name, -renderer.width(name) / 2, 32, CommonColors.RED);

                    // 如果目标已经死亡，显示等待状态
                    if (executionerPlayerComponent.won) {
                        Component status = Component.literal("目标已死亡");
                        context.drawString(renderer, status, -renderer.width(status) / 2, 44, CommonColors.YELLOW);
                    }

                    context.pose().popPose();
                } else if (!executionerPlayerComponent.targetSelected) {
                    // 显示需要选择目标的提示
                    context.pose().pushPose();
                    context.pose().translate((float) context.guiWidth() / 2.0F, (float) context.guiHeight() / 2.0F + 6.0F, 0.0F);
                    context.pose().scale(0.6F, 0.6F, 1.0F);

                    Component prompt = Component.literal("打开背包选择目标");
                    context.drawString(renderer, prompt, -renderer.width(prompt) / 2, 32, CommonColors.YELLOW);

                    context.pose().popPose();
                }
            }
        }
        }

    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDisplayName()Lnet/minecraft/network/chat/Component;"))
    private static void executionerGetTarget(Font renderer, LocalPlayer player, GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci, @Local Player target) {
        NoellesrolesClient.target = target;
    }
}

