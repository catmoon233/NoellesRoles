package org.agmas.noellesroles.client;

import java.awt.Color;

import org.agmas.noellesroles.AttendantHandler;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.NoellesRolesAbilityPlayerComponent;
import org.agmas.noellesroles.component.RecorderPlayerComponent;
import org.agmas.noellesroles.component.BloodFeudistPlayerComponent;
import org.agmas.noellesroles.component.ClockmakerPlayerComponent;
import org.agmas.noellesroles.entity.WheelchairEntity;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.commander.CommanderHudRender;
import org.agmas.noellesroles.roles.fortuneteller.FortunetellerPlayerComponent;
import org.agmas.noellesroles.roles.thief.ThiefPlayerComponent;

import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.BartenderPlayerComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientHudRenderer {

    public static void registerRenderersEvent() {
        CommanderHudRender.register();
        WayfarerHudRenderer.registerRendererEvent();
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            // 记录员
            var client = Minecraft.getInstance();
            if (client == null)
                return;
            if (client.player == null)
                return;
            if (TMMClient.gameComponent == null
                    || !TMMClient.gameComponent.isRole(client.player, ModRoles.RECORDER)) {
                return;
            }
            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();
            var font = client.font;
            int yOffset = screenHeight - 10 - font.lineHeight; // 右下角
            int xOffset = screenWidth - 10; // 距离右边缘
            var abpc = RecorderPlayerComponent.KEY.get(client.player);
            // hud.noellesroles.recorder.process
            Component text = Component
                    .translatable("hud.noellesroles.recorder.requirement",
                            abpc.requiredCorrectCount)
                    .withStyle(ChatFormatting.GOLD);
            Component text2 = Component
                    .translatable("hud.noellesroles.recorder.process",
                            abpc.getCorrectGuesses(), abpc.requiredCorrectCount)
                    .withStyle(ChatFormatting.YELLOW);
            guiGraphics.drawString(font, text2, xOffset - font.width(text2), yOffset - font.lineHeight - 4,
                    Color.WHITE.getRGB());
            guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset - font.lineHeight * 2 - 8,
                    Color.WHITE.getRGB());
            return;
        });
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            var client = Minecraft.getInstance();
            if (client == null)
                return;
            if (client.player == null)
                return;
            if (TMMClient.gameComponent == null
                    || !TMMClient.gameComponent.isRole(client.player, ModRoles.CLOCKMAKER)) {
                return;
            }
            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();
            var font = client.font;
            int yOffset = screenHeight - 10 - font.lineHeight; // 右下角
            int xOffset = screenWidth - 10; // 距离右边缘
            var abpc = ClockmakerPlayerComponent.KEY.get(client.player);
            Component text = Component
                    .translatable("hud.noellesroles.clockmaker.use",
                            NoellesrolesClient.abilityBind.getTranslatedKeyMessage())
                    .withStyle(ChatFormatting.GOLD);
            if (abpc.isUsingSkill) {
                text = Component.translatable("hud.noellesroles.clockmaker.already_using")
                        .withStyle(ChatFormatting.DARK_AQUA);
            }
            // 按下技能键可花费125金币，减少游戏时间45秒并使世界时间加快2000tick。
            guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset - font.lineHeight - 4,
                    Color.WHITE.getRGB());
            return;
        });
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            // 渲染清道夫的提示
            var client = Minecraft.getInstance();
            if (client == null)
                return;
            if (client.player == null)
                return;
            if (TMMClient.gameComponent == null || !TMMClient.gameComponent.isRole(client.player, ModRoles.CLEANER)) {
                return;
            }
            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();
            var font = client.font;
            int yOffset = screenHeight - 10 - font.lineHeight; // 右下角
            int xOffset = screenWidth - 10; // 距离右边缘
            var abpc = NoellesRolesAbilityPlayerComponent.KEY.get(client.player);
            if (abpc.cooldown > 0) {
                var text = Component
                        .translatable("hud.cleaner.cooldown", abpc.cooldown / 20)
                        .withStyle(ChatFormatting.AQUA);
                guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset - font.lineHeight - 4,
                        Color.WHITE.getRGB());
            } else {
                var text = Component
                        .translatable("hud.cleaner.ready", NoellesrolesClient.abilityBind.getTranslatedKeyMessage())
                        .withStyle(ChatFormatting.AQUA);
                guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset - font.lineHeight - 4,
                        Color.WHITE.getRGB());
            }
            return;
        });
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            // 渲染风精灵的提示
            var client = Minecraft.getInstance();
            if (client == null)
                return;
            if (client.player == null)
                return;
            if (TMMClient.gameComponent == null
                    || !TMMClient.gameComponent.isRole(client.player, ModRoles.WIND_YAOSE)) {
                return;
            }
            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();
            var font = client.font;
            int yOffset = screenHeight - 10 - font.lineHeight; // 右下角
            int xOffset = screenWidth - 10; // 距离右边缘
            var abpc = NoellesRolesAbilityPlayerComponent.KEY.get(client.player);
            if (abpc.cooldown > 0) {
                var text = Component
                        .translatable("hud.wind_yaose.cooldown", abpc.cooldown / 20)
                        .withStyle(ChatFormatting.AQUA);
                guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset - font.lineHeight - 4,
                        Color.WHITE.getRGB());
            } else {
                var text = Component
                        .translatable("hud.wind_yaose.ready", NoellesrolesClient.abilityBind.getTranslatedKeyMessage())
                        .withStyle(ChatFormatting.AQUA);
                guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset - font.lineHeight - 4,
                        Color.WHITE.getRGB());
            }
            return;
        });

        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            // 渲染算命大师的提示
            var client = Minecraft.getInstance();
            if (client == null)
                return;
            if (client.player == null)
                return;
            if (TMMClient.gameComponent == null
                    || !TMMClient.gameComponent.isRole(client.player, ModRoles.FORTUNETELLER)) {
                return;
            }
            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();
            var font = client.font;
            int yOffset = screenHeight - 10 - font.lineHeight; // 右下角
            int xOffset = screenWidth - 10; // 距离右边缘
            var abpc = NoellesRolesAbilityPlayerComponent.KEY.get(client.player);
            var fpc = FortunetellerPlayerComponent.KEY.get(client.player);
            if (!fpc.protectedPlayers.isEmpty()) {
                int dy = yOffset - font.lineHeight * 2 - 12;
                for (var po : fpc.protectedPlayers) {
                    var pl = client.level.getPlayerByUUID(po.player);
                    if (pl == null)
                        continue;
                    var text = Component
                            .translatable("hud.fortuneteller.protecting_line",
                                    Component.literal(pl.getDisplayName().getString()).withStyle(ChatFormatting.GREEN),
                                    Component.literal((po.time / 20) + "s").withStyle(ChatFormatting.YELLOW))
                            .withStyle(ChatFormatting.GOLD);
                    guiGraphics.drawString(font, text, xOffset - font.width(text), dy,
                            Color.WHITE.getRGB());
                    dy = dy - 2 - font.lineHeight;
                }
                var text = Component
                        .translatable("hud.fortuneteller.protecting_above")
                        .withStyle(ChatFormatting.AQUA);
                guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset - font.lineHeight - 4,
                        Color.WHITE.getRGB());
            }
            if (abpc.cooldown > 0) {
                var text = Component
                        .translatable("hud.fortuneteller.cooldown", abpc.cooldown / 20)
                        .withStyle(ChatFormatting.YELLOW);
                guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset, Color.WHITE.getRGB());
            } else {
                var text = Component
                        .translatable("hud.fortuneteller.ready",
                                NoellesrolesClient.abilityBind.getTranslatedKeyMessage())
                        .withStyle(ChatFormatting.YELLOW);
                guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset, Color.WHITE.getRGB());
            }

            return;
        });
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            // 渲染老人的提示
            var client = Minecraft.getInstance();
            if (client == null)
                return;
            if (client.player == null)
                return;
            if (TMMClient.gameComponent == null || !TMMClient.gameComponent.isRole(client.player, ModRoles.OLDMAN)) {
                return;
            }
            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();
            var font = client.font;
            int yOffset = screenHeight - 10 - font.lineHeight; // 右下角
            int xOffset = screenWidth - 10; // 距离右边缘
            if (client.player.getVehicle() != null && client.player.getVehicle() instanceof WheelchairEntity) {
                var text = Component
                        .translatable("hud.oldman.get_back", NoellesrolesClient.abilityBind.getTranslatedKeyMessage())
                        .withStyle(ChatFormatting.AQUA);
                guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset, Color.WHITE.getRGB());
            }

            return;
        });
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            // 渲染酒保的提示
            var client = Minecraft.getInstance();
            if (client == null)
                return;
            if (client.player == null)
                return;
            if (TMMClient.gameComponent != null && (TMMClient.gameComponent.isRole(client.player, ModRoles.BARTENDER)
                    || TMMClient.gameComponent.isRole(client.player, TMMRoles.LOOSE_END))) {
                var comc = BartenderPlayerComponent.KEY.maybeGet(client.player).orElse(null);
                if (comc == null)
                    return;
                if (comc.getArmor() <= 0)
                    return;
                int screenWidth = guiGraphics.guiWidth();
                int screenHeight = guiGraphics.guiHeight();
                var font = client.font;
                int yOffset = screenHeight - 10 - font.lineHeight; // 右下角
                int xOffset = screenWidth - 10; // 距离右边缘
                var text = Component.translatable("hud.bartender.has_armor", comc.getArmor())
                        .withStyle(ChatFormatting.GOLD);
                guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset, Color.WHITE.getRGB());
                return;
            }

            if (TMMClient.gameComponent != null && TMMClient.gameComponent.isRole(client.player, ModRoles.ATTENDANT)) {
                var comc = NoellesRolesAbilityPlayerComponent.KEY.maybeGet(client.player).orElse(null);
                if (comc == null)
                    return;
                int screenWidth = guiGraphics.guiWidth();
                int screenHeight = guiGraphics.guiHeight();
                var font = client.font;
                int yOffset = screenHeight - 10 - font.lineHeight; // 右下角
                int xOffset = screenWidth - 10; // 距离右边缘
                var text = Component.literal("");
                if (comc.cooldown <= 0) {
                    text.append(Component.translatable("hud.noellesroles.attendant.available",
                            Component.keybind("key.noellesroles.ability"), AttendantHandler.area_distance)
                            .withStyle(ChatFormatting.GOLD));
                } else {
                    text.append(Component.translatable("hud.noellesroles.attendant.cooldown", (comc.cooldown / 20))
                            .withStyle(ChatFormatting.RED));
                }

                guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset, Color.WHITE.getRGB());
                return;
            }
        });

        // 小偷HUD
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            var client = Minecraft.getInstance();
            if (client == null)
                return;
            if (client.player == null)
                return;
            if (TMMClient.gameComponent == null || !TMMClient.gameComponent.isRole(client.player, ModRoles.THIEF)) {
                return;
            }

            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();
            var font = client.font;
            int yOffset = screenHeight - 10 - font.lineHeight; // 右下角
            int xOffset = screenWidth - 10; // 距离右边缘

            var thiefComponent = ThiefPlayerComponent.KEY.maybeGet(client.player).orElse(null);
            if (thiefComponent == null)
                return;

            // 显示当前模式
            Component progress = Component.literal("");
            var shopC = PlayerShopComponent.KEY.get(client.player);
            progress = Component.translatable("message.thief.honor_cost", shopC.balance, thiefComponent.honorCost)
                    .withStyle(ChatFormatting.GOLD);
            Component modeText;
            if (thiefComponent.currentMode == ThiefPlayerComponent.MODE_STEAL_MONEY) {
                modeText = Component.translatable("hud.thief.mode.money").withStyle(ChatFormatting.GOLD);
            } else {
                modeText = Component.translatable("hud.thief.mode.item").withStyle(ChatFormatting.AQUA);
            }

            // 显示冷却或就绪状态
            int dy = yOffset - font.lineHeight - 4;
            if (thiefComponent.cooldown > 0) {
                var cdText = Component.translatable("hud.thief.cooldown", thiefComponent.cooldown / 20)
                        .withStyle(ChatFormatting.RED);
                guiGraphics.drawString(font, cdText, xOffset - font.width(cdText), dy, Color.WHITE.getRGB());
                dy -= font.lineHeight;
            } else {
                var readyText = Component
                        .translatable("hud.thief.ready", NoellesrolesClient.abilityBind.getTranslatedKeyMessage())
                        .withStyle(ChatFormatting.GREEN);
                guiGraphics.drawString(font, readyText, xOffset - font.width(readyText), dy, Color.WHITE.getRGB());
                dy -= font.lineHeight;
            }

            // 显示模式信息
            var modeInfo = Component.translatable("hud.thief.current_mode").withStyle(ChatFormatting.WHITE);
            guiGraphics.drawString(font, modeInfo, xOffset - font.width(modeInfo) - font.width(modeText), dy,
                    Color.WHITE.getRGB());
            guiGraphics.drawString(font, modeText, xOffset - font.width(modeText), dy, Color.WHITE.getRGB());

            dy -= font.lineHeight + 8;

            guiGraphics.drawString(font, progress, xOffset - font.width(progress), dy, Color.WHITE.getRGB());
        });

        // 仇杀客HUD
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            var client = Minecraft.getInstance();
            if (client == null)
                return;
            if (client.player == null)
                return;
            if (TMMClient.gameComponent == null
                    || !TMMClient.gameComponent.isRole(client.player, ModRoles.BLOOD_FEUDIST)) {
                return;
            }

            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();
            var font = client.font;
            int yOffset = screenHeight - 10 - font.lineHeight; // 右下角
            int xOffset = screenWidth - 10; // 距离右边缘

            BloodFeudistPlayerComponent bfComponent = ModComponents.BLOOD_FEUDIST.maybeGet(client.player).orElse(null);
            if (bfComponent == null)
                return;

            int dy = yOffset;

            // 显示误杀人数
            var killText = Component
                    .translatable("hud.blood_feudist.accidental_kills", bfComponent.getAccidentalKillCount())
                    .withStyle(ChatFormatting.RED);
            guiGraphics.drawString(font, killText, xOffset - font.width(killText), dy, Color.WHITE.getRGB());
            dy -= font.lineHeight + 2;

            // 显示速度状态
            if (bfComponent.hasSpeed1() || bfComponent.hasSpeed2()) {
                Component speedLabel = bfComponent.hasSpeed2() ? Component.translatable("hud.blood_feudist.speed2")
                        : Component.translatable("hud.blood_feudist.speed1");
                Component speedStatus = bfComponent.isSpeedEnabled()
                        ? Component.translatable("hud.blood_feudist.enabled").withStyle(ChatFormatting.GREEN)
                        : Component.translatable("hud.blood_feudist.disabled").withStyle(ChatFormatting.GRAY);
                Component speedText = Component.literal("").append(speedLabel).append(speedStatus);
                guiGraphics.drawString(font, speedText, xOffset - font.width(speedText), dy, Color.WHITE.getRGB());
                dy -= font.lineHeight + 2;
            }

            // 显示急迫状态
            if (bfComponent.hasHaste2()) {
                Component hasteLabel = Component.translatable("hud.blood_feudist.haste2");
                Component hasteStatus = bfComponent.isHasteEnabled()
                        ? Component.translatable("hud.blood_feudist.enabled").withStyle(ChatFormatting.GREEN)
                        : Component.translatable("hud.blood_feudist.disabled").withStyle(ChatFormatting.GRAY);
                Component hasteText = Component.literal("").append(hasteLabel).append(hasteStatus);
                guiGraphics.drawString(font, hasteText, xOffset - font.width(hasteText), dy, Color.WHITE.getRGB());
                dy -= font.lineHeight + 2;
            }

            // 显示技能提示
            var readyText = Component
                    .translatable("hud.blood_feudist.toggle_effects",
                            NoellesrolesClient.abilityBind.getTranslatedKeyMessage())
                    .withStyle(ChatFormatting.YELLOW);
            guiGraphics.drawString(font, readyText, xOffset - font.width(readyText), dy, Color.WHITE.getRGB());
        });
    }

}
