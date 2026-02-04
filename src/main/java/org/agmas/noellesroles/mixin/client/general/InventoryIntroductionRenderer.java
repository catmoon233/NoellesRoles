// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package org.agmas.noellesroles.mixin.client.general;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.trainmurdermystery.util.TooltipUtil;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ LimitedInventoryScreen.class })
public class InventoryIntroductionRenderer {
   public InventoryIntroductionRenderer() {
   }

   @Inject(method = { "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V" }, at = { @At("TAIL") })
   public void render(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         GameWorldComponent gameWorldComponent = TMMClient.gameComponent;
         if (gameWorldComponent != null) {
            // Role
            Role role = gameWorldComponent.getRole(player);
            if (role != null) {
               String roleName = role.getIdentifier().getPath();
               if (roleName != null) {
                  Font font = Minecraft.getInstance().font;
                  int x = 10;
                  int y = 10;
                  float scale = 0.8F;
                  Component roleNameComponent = Component.translatable("announcement.role." + roleName)
                        .withStyle(ChatFormatting.BOLD);
                  Component roleInfoComponent = Component.translatable("info.screen.roleid." + roleName);
                  PoseStack poseStack = context.pose();
                  poseStack.pushPose();
                  poseStack.scale(scale, scale, 1.0F);
                  float scaledX = (float) x / scale;
                  float scaledY = (float) y / scale;
                  this.renderScaledTextWithShadow(context, font, roleNameComponent, scaledX, scaledY, scale, 16777215,
                        4210752);
                  Objects.requireNonNull(font);
                  int roleNameHeight = (int) (9.0F * scale);
                  int currentY = y + roleNameHeight + 2;
                  List<Component> infoLines = TooltipUtil.sprit(roleInfoComponent);

                  for (Iterator<Component> var22 = infoLines.iterator(); var22
                        .hasNext(); currentY += (int) (9.0F * scale) + 2) {
                     Component line = (Component) var22.next();
                     float lineY = (float) currentY / scale;
                     context.drawString(font, line, (int) scaledX, (int) lineY, 11184810);
                     Objects.requireNonNull(font);
                  }

                  poseStack.popPose();
                  int infoLineCount = infoLines.size();
                  Objects.requireNonNull(font);
                  int var10000 = (int) (9.0F * scale);
                  Objects.requireNonNull(font);
                  int totalHeight = var10000 + infoLineCount * (int) (9.0F * scale) + infoLineCount * 2 + 2;
                  int scaledNameWidth = (int) ((float) font.width(roleNameComponent) * scale);
                  int maxInfoWidth = infoLines.stream().mapToInt((component) -> {
                     return (int) ((float) font.width(component) * scale);
                  }).max().orElse(0);
                  int maxWidth = Math.max(scaledNameWidth, maxInfoWidth);
                  this.drawScaledBackground(context, x, y, maxWidth, totalHeight);
               }
            }
            // Modifier
            WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(player.level());
            if (worldModifierComponent != null) {
               var modifiers = worldModifierComponent.getModifiers(player);
               if (modifiers != null && modifiers.size() > 0) {
                  Font font = Minecraft.getInstance().font;
                  float scale = 0.8F;
                  int x = (int) ((float) context.guiWidth()) - 10;
                  int y = 20;
                  for (var modifier : modifiers) {
                     Component modifierNameComponent = modifier.getName()
                           .withStyle(ChatFormatting.BOLD);
                     Component modifierInfoComponent = Component
                           .translatable("info.screen.modifier." + modifier.identifier().getPath());
                     PoseStack poseStack = context.pose();
                     poseStack.pushPose();
                     poseStack.scale(scale, scale, 1.0F);
                     float scaledX = (float) x / scale;
                     float scaledY = (float) y / scale;

                     Objects.requireNonNull(font);
                     int modifierNameHeight = (int) (9.0F * scale);
                     int currentY = y + modifierNameHeight + 2;
                     List<Component> infoLines = TooltipUtil.sprit(modifierInfoComponent);

                     int infoLineCount = infoLines.size();
                     Objects.requireNonNull(font);
                     int var10000 = (int) (9.0F * scale);
                     Objects.requireNonNull(font);
                     int totalHeight = var10000 + infoLineCount * (int) (9.0F * scale) + infoLineCount * 2 + 2;
                     int scaledNameWidth = (int) ((float) font.width(modifierNameComponent) * scale);
                     int maxInfoWidth = infoLines.stream().mapToInt((component) -> {
                        return (int) ((float) font.width(component) * scale);
                     }).max().orElse(0);
                     int maxWidth = Math.max(scaledNameWidth, maxInfoWidth);
                     this.renderScaledTextWithShadow(context, font, modifierNameComponent, scaledX - font.width(modifierNameComponent),
                           scaledY, scale,
                           16777215,
                           4210752);
                     for (Iterator<Component> var22 = infoLines.iterator(); var22
                           .hasNext(); currentY += (int) (9.0F * scale) + 2) {
                        Component line = (Component) var22.next();
                        float lineY = (float) currentY / scale;
                        context.drawString(font, line, (int) (scaledX - font.width(line)), (int) lineY, 11184810);
                        Objects.requireNonNull(font);
                     }
                     poseStack.popPose();

                     this.drawScaledBackground(context, x - maxWidth, y, maxWidth, totalHeight);
                     y += totalHeight + 10;
                  }

               }
            }
         }
      }
   }

   private void renderScaledTextWithShadow(GuiGraphics context, Font font, Component text, float x, float y,
         float scale, int textColor, int shadowColor) {
      PoseStack poseStack = context.pose();
      poseStack.pushPose();
      context.drawString(font, text, (int) (x + 1.0F / scale), (int) (y + 1.0F / scale), shadowColor);
      context.drawString(font, text, (int) x, (int) y, textColor);
      poseStack.popPose();
   }

   private void drawScaledBackground(GuiGraphics context, int x, int y, int width, int height) {
      int padding = 3;
      int borderThickness = 1;
      int bgX = x - padding;
      int bgY = y - padding;
      int bgWidth = width + padding * 2;
      int bgHeight = height + padding * 2;
      int backgroundColor = Integer.MIN_VALUE;
      context.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, backgroundColor);
      int borderColor = -10066330;
      context.fill(bgX, bgY, bgX + bgWidth, bgY + borderThickness, borderColor);
      context.fill(bgX, bgY + bgHeight - borderThickness, bgX + bgWidth, bgY + bgHeight, borderColor);
      context.fill(bgX, bgY, bgX + borderThickness, bgY + bgHeight, borderColor);
      context.fill(bgX + bgWidth - borderThickness, bgY, bgX + bgWidth, bgY + bgHeight, borderColor);
   }
}
