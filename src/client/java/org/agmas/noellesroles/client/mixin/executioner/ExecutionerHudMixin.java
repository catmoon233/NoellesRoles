package org.agmas.noellesroles.client.mixin.executioner;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.gui.RoleNameRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.client.HarpymodloaderClient;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.agmas.noellesroles.coroner.BodyDeathReasonComponent;
import org.agmas.noellesroles.executioner.ExecutionerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RoleNameRenderer.class)
public abstract class ExecutionerHudMixin {

    @Shadow private static float nametagAlpha;

    @Shadow private static Text nametag;


    @Inject(method = "renderHud", at = @At("HEAD"))
    private static void executionerHudRenderer(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());

        // 检查是否是Executioner角色且存活
        if (MinecraftClient.getInstance().player != null && gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.EXECUTIONER) && TMMClient.isPlayerAliveAndInSurvival()) {
            // 检查是否已经转变为杀手
            if (!gameWorldComponent.getRole(MinecraftClient.getInstance().player).canUseKiller()) {
                ExecutionerPlayerComponent executionerPlayerComponent = ExecutionerPlayerComponent.KEY.get(player);

                // 检查是否有选定的目标
                if (executionerPlayerComponent.target != null && executionerPlayerComponent.targetSelected) {
                    var playerListEntry = MinecraftClient.getInstance().player.networkHandler.getPlayerListEntry(executionerPlayerComponent.target);
                    if (playerListEntry == null) return;

                    context.getMatrices().push();
                    context.getMatrices().translate((float) context.getScaledWindowWidth() / 2.0F, (float) context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
                    context.getMatrices().scale(0.6F, 0.6F, 1.0F);

                    // 显示目标玩家名称
                    Text name = Text.literal("目标: " + playerListEntry.getProfile().getName());
                    context.drawTextWithShadow(renderer, name, -renderer.getWidth(name) / 2, 32, Colors.RED);

                    // 如果目标已经死亡，显示等待状态
                    if (executionerPlayerComponent.won) {
                        Text status = Text.literal("目标已死亡");
                        context.drawTextWithShadow(renderer, status, -renderer.getWidth(status) / 2, 44, Colors.YELLOW);
                    }

                    context.getMatrices().pop();
                } else if (!executionerPlayerComponent.targetSelected) {
                    // 显示需要选择目标的提示
                    context.getMatrices().push();
                    context.getMatrices().translate((float) context.getScaledWindowWidth() / 2.0F, (float) context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
                    context.getMatrices().scale(0.6F, 0.6F, 1.0F);

                    Text prompt = Text.literal("打开背包选择目标");
                    context.drawTextWithShadow(renderer, prompt, -renderer.getWidth(prompt) / 2, 32, Colors.YELLOW);

                    context.getMatrices().pop();
                }
            }
        }
        }

    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getDisplayName()Lnet/minecraft/text/Text;"))
    private static void executionerGetTarget(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci, @Local PlayerEntity target) {
        NoellesrolesClient.target = target;
    }
}

