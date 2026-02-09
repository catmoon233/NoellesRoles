package org.agmas.noellesroles.mixin.client.nianshou;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.agmas.noellesroles.component.NianShouPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 年兽HUD，显示红包数量
 */
@Mixin(targets = {"dev.doctor4t.trainmurdermystery.client.gui.LayeredRender"}, remap = false)
public class NianShouHudMixin {

    @Inject(method = "render", at = @At("RETURN"))
    private void renderNianShouHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        // 检查是否是年兽
        if (net.minecraft.client.Minecraft.getInstance().player == null)
            return;

        var player = net.minecraft.client.Minecraft.getInstance().player;
        var gameWorld = GameWorldComponent.KEY.get(player.level());

        if (gameWorld == null || !gameWorld.isRole(player, ModRoles.NIAN_SHOU))
            return;

        // 获取红包组件
        var nianShouComponent = NianShouPlayerComponent.KEY.get(player);

        if (nianShouComponent == null)
            return;

        // 渲染红包数量
        int redPacketCount = nianShouComponent.getRedPacketCount();

        var font = net.minecraft.client.Minecraft.getInstance().font;
        int x = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 120;
        int y = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight() - 50;

        String text = net.minecraft.network.chat.Component.translatable("hud.noellesroles.nianshou.red_packets", redPacketCount).getString();

        guiGraphics.drawString(font, text, x, y, 0xFFD700, true);
    }
}
