package org.agmas.noellesroles.mixin.client.roles;

import org.agmas.noellesroles.component.MaChenXuPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;

import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 马晨絮 HUD Mixin
 * 显示马晨絮的状态：
 * - 当前阶段
 * - 累计SAN掉落
 * - 进化进度
 * - 里世界状态
 * - 鬼术状态
 */
@Mixin(Gui.class)
public class MaChenXuHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void renderMaChenXuHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null)
            return;
        if (client.player.isSpectator())
            return;
        if (TMMClient.gameComponent == null)
            return;
        var role = TMMClient.gameComponent.getRole(client.player);
        if (role == null)
            return;
        if (!role.identifier().getPath().equals(ModRoles.MA_CHEN_XU.identifier().getPath())) {
            return;
        }

        // 获取马晨絮组件
        MaChenXuPlayerComponent component = MaChenXuPlayerComponent.KEY.get(client.player);
        if (component == null)
            return;

        // 检查玩家是否存活
        if (!GameFunctions.isPlayerAliveAndSurvival(client.player))
            return;

        // 渲染位置 - 左下角
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int x = 10;
        int y = screenHeight - 120;

        Font textRenderer = client.font;

        // 阶段显示
        String phaseName = switch (component.stage) {
            case 1 -> "初级鬼";
            case 2 -> "中级鬼";
            case 3 -> "高级鬼";
            case 4 -> "极致鬼";
            default -> "未知";
        };
        Component phaseText = Component.translatable("hud.noellesroles.ma_chen_xu.phase", phaseName)
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
        context.drawString(textRenderer, phaseText, x, y, 0xFFFFFF);
        y += 12;

        // 累计SAN掉落
        Component sanText = Component.translatable("hud.noellesroles.ma_chen_xu.total_san_loss",
                component.totalSanLoss).withStyle(ChatFormatting.RED);
        context.drawString(textRenderer, sanText, x, y, 0xFFFFFF);
        y += 12;

        // 进化进度
        int nextThreshold = switch (component.stage) {
            case 1 -> 50;
            case 2 -> 120;
            case 3 -> 200;
            default -> -1;
        };

        if (nextThreshold > 0) {
            Component progressText = Component.translatable("hud.noellesroles.ma_chen_xu.evolution_progress",
                    component.totalSanLoss, nextThreshold).withStyle(ChatFormatting.YELLOW);
            context.drawString(textRenderer, progressText, x, y, 0xFFFFFF);
            y += 12;
        }

        // 里世界状态
        if (component.otherworldActive) {
            Component liShiJieText = Component.translatable("hud.noellesroles.ma_chen_xu.li_shi_jie_active",
                    component.otherworldDuration / 20).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
            context.drawString(textRenderer, liShiJieText, x, y, 0xFFFFFF);
            y += 12;

            // 下雨[狂热]状态
            if (component.frenzyRainActive) {
                Component frenzyRainText = Component.translatable("hud.noellesroles.ma_chen_xu.frenzy_rain_active",
                        component.frenzyRainDuration / 20).withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD);
                context.drawString(textRenderer, frenzyRainText, x, y, 0xFFFFFF);
                y += 12;
            }

            // 下雨[狂热]冷却状态
            if (component.frenzyRainCooldown > 0) {
                Component frenzyRainCooldownText = Component
                        .translatable("gui.noellesroles.ma_chen_xu.frenzy_rain_cooldown",
                                component.frenzyRainCooldown / 20)
                        .withStyle(ChatFormatting.RED);
                context.drawString(textRenderer, frenzyRainCooldownText, x, y, 0xFFFFFF);
                y += 12;
            }
        }

        // 鬼术状态
        if (component.swiftWindActive) {
            Component swiftWindText = Component.translatable("hud.noellesroles.ma_chen_xu.swift_wind_active",
                    component.swiftWindDuration / 20).withStyle(ChatFormatting.AQUA);
            context.drawString(textRenderer, swiftWindText, x, y, 0xFFFFFF);
            y += 12;
        }

        if (component.spiritWalkActive) {
            Component spiritWalkText = Component.translatable("hud.noellesroles.ma_chen_xu.spirit_walk_active",
                    component.spiritWalkDuration / 20).withStyle(ChatFormatting.LIGHT_PURPLE);
            context.drawString(textRenderer, spiritWalkText, x, y, 0xFFFFFF);
            y += 12;
        }

        if (component.puppetShowActive) {
            Component puppetShowText = Component.translatable("hud.noellesroles.ma_chen_xu.puppet_show_active",
                    component.puppetShowDuration / 20).withStyle(ChatFormatting.DARK_PURPLE);
            context.drawString(textRenderer, puppetShowText, x, y, 0xFFFFFF);
            y += 12;
        }

        // 掠风充能进度
        if (component.ghostSkills.contains("swift_wind") && component.swiftWindChargeTime < 300) {
            int progress = (component.swiftWindChargeTime * 100) / 300;
            Component chargeText = Component.translatable("hud.noellesroles.ma_chen_xu.swift_wind_charge", progress)
                    .withStyle(ChatFormatting.GREEN);
            context.drawString(textRenderer, chargeText, x, y, 0xFFFFFF);
            y += 12;
        }

        // 伪摹使用状态
        if (component.falseMimicryUsed) {
            Component mimicryText = Component.translatable("hud.noellesroles.ma_chen_xu.false_mimicry_used")
                    .withStyle(ChatFormatting.GOLD);
            context.drawString(textRenderer, mimicryText, x, y, 0xFFFFFF);
        }
    }
}