package org.agmas.noellesroles.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.item.BombItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class BomberHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void renderBombTimer(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null)
            return;

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.is(ModItems.BOMB)) {
            CustomData customData = mainHandItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();

            if (tag.contains(BombItem.TIMER_KEY)) {
                int timer = tag.getInt(BombItem.TIMER_KEY);
                String text = String.format("%.1fs", timer / 20.0f);

                int width = client.getWindow().getGuiScaledWidth();
                int height = client.getWindow().getGuiScaledHeight();

                guiGraphics.drawCenteredString(client.font, text, width / 2, height / 2 - 20, 0xFF0000);
            }
        }
    }
}