package org.agmas.noellesroles.mixin.client;

import dev.doctor4t.trainmurdermystery.api.Role;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.PuppeteerPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 傀儡师 HUD 显示
 * 
 * 显示：
 * - 当前阶段（收集者/傀儡大师）
 * - 收集的尸体数量和阈值
 * - 收集冷却/技能冷却
 * - 假人操控剩余时间
 */
@Mixin(InGameHud.class)
public abstract class PuppeteerHudMixin {

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void renderPuppeteerHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        // 获取傀儡师组件
        PuppeteerPlayerComponent puppeteerComp = ModComponents.PUPPETEER.get(client.player);

        // 检查玩家是否是傀儡师（包括操控假人时角色临时变更的情况）
        // 操控假人时角色会变成其他杀手，但 isActivePuppeteer() 仍然返回 true
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.world);
        final var role = gameWorld.getRole(client.player);
        if (role==null)return;
        if (!role.getIdentifier().equals(ModRoles.PUPPETEER_ID) && !puppeteerComp.isActivePuppeteer()) return;

        // 确保傀儡师已激活
        if (!puppeteerComp.isActivePuppeteer()) return;

        TextRenderer textRenderer = this.getTextRenderer();
        int screenHeight = client.getWindow().getScaledHeight();

        // 左下角位置
        int baseX = 10;
        int baseY = screenHeight - 80;

        // ==================== 显示角色名称 ====================
        Text titleText = Text.translatable("hud.noellesroles.puppeteer.title")
            .formatted(Formatting.DARK_PURPLE, Formatting.BOLD);
        context.drawTextWithShadow(textRenderer, titleText, baseX, baseY - 25, 0x9400D3);

        // ==================== 显示阶段 ====================
        Text phaseText;
        if (puppeteerComp.phase == 1) {
            phaseText = Text.translatable("hud.noellesroles.puppeteer.phase1")
                .formatted(Formatting.LIGHT_PURPLE);
        } else {
            phaseText = Text.translatable("hud.noellesroles.puppeteer.phase2")
                .formatted(Formatting.DARK_PURPLE, Formatting.BOLD);
        }
        context.drawTextWithShadow(textRenderer, phaseText, baseX, baseY - 12, 0xDA70D6);

        // ==================== 阶段一：收集者模式 ====================
        if (puppeteerComp.phase == 1) {
            // 计算阈值（总人数/6）
            int totalPlayers = client.world.getPlayers().size();
            int threshold = Math.max(1, totalPlayers / 6);

            // 显示收集进度
            Text bodiesText = Text.translatable("hud.noellesroles.puppeteer.bodies_collected",
                puppeteerComp.collectedBodies, threshold)
                .formatted(puppeteerComp.collectedBodies >= threshold ? Formatting.GREEN : Formatting.GRAY);
            context.drawTextWithShadow(textRenderer, bodiesText, baseX, baseY,
                puppeteerComp.collectedBodies >= threshold ? 0x55FF55 : 0xAAAAAA);

            // 显示收集冷却
            Text collectText;
            if (puppeteerComp.collectCooldown > 0) {
                collectText = Text.translatable("hud.noellesroles.puppeteer.collect_cooldown",
                    String.format("%.1f", puppeteerComp.getCollectCooldownSeconds()))
                    .formatted(Formatting.RED);
            } else {
                collectText = Text.translatable("hud.noellesroles.puppeteer.collect_ready")
                    .formatted(Formatting.GREEN);
            }
            context.drawTextWithShadow(textRenderer, collectText, baseX, baseY + 12,
                puppeteerComp.collectCooldown > 0 ? 0xFF5555 : 0x55FF55);

        // ==================== 阶段二：傀儡大师模式 ====================
        } else if (puppeteerComp.phase == 2) {
            // 如果正在操控假人
            if (puppeteerComp.isControllingPuppet) {
                // 显示操控剩余时间
                Text controlText = Text.translatable("hud.noellesroles.puppeteer.controlling",
                    String.format("%.0f", puppeteerComp.getPuppetControlSeconds()))
                    .formatted(Formatting.YELLOW, Formatting.BOLD);
                context.drawTextWithShadow(textRenderer, controlText, baseX, baseY, 0xFFFF00);

                // 显示返回本体提示
                Text returnHint = Text.literal("按 G 返回本体")
                    .formatted(Formatting.GOLD);
                context.drawTextWithShadow(textRenderer, returnHint, baseX, baseY + 12, 0xFFA500);
            } else {
                // 显示本体模式标识
                Text bodyModeText = Text.translatable("hud.noellesroles.puppeteer.body_mode")
                    .formatted(Formatting.GRAY);
                context.drawTextWithShadow(textRenderer, bodyModeText, baseX, baseY, 0x888888);

                // 显示技能冷却或就绪状态
                Text abilityText;
                int remainingPuppets = puppeteerComp.getRemainingPuppetUses();

                if (puppeteerComp.abilityCooldown > 0) {
                    abilityText = Text.translatable("hud.noellesroles.puppeteer.puppet_cooldown",
                        String.format("%.0f", puppeteerComp.getAbilityCooldownSeconds()))
                        .formatted(Formatting.RED);
                } else if (remainingPuppets > 0) {
                    abilityText = Text.translatable("hud.noellesroles.puppeteer.puppet_ready", remainingPuppets)
                        .formatted(Formatting.GREEN);
                } else {
                    abilityText = Text.translatable("hud.noellesroles.puppeteer.no_puppets")
                        .formatted(Formatting.DARK_GRAY);
                }
                context.drawTextWithShadow(textRenderer, abilityText, baseX, baseY + 12,
                    puppeteerComp.abilityCooldown > 0 ? 0xFF5555 : (remainingPuppets > 0 ? 0x55FF55 : 0x555555));
            }
        }
    }
}