package org.agmas.noellesroles.mixin.client.coroner;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.gui.RoleNameRenderer;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.coroner.BodyDeathReasonComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RoleNameRenderer.class)
public abstract class CoronerHudMixin {

    @Shadow private static float nametagAlpha;

    @Shadow private static Component nametag;


    @Inject(method = "renderHud", at = @At("TAIL"))
    private static void coronerRoleNameRenderer(Font renderer, LocalPlayer player, GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.level());
        if (NoellesrolesClient.targetBody != null) {
            if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.CORONER) || gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.VULTURE) || TMMClient.isPlayerSpectatingOrCreative()) {

                context.pose().pushPose();
                context.pose().translate((float)context.guiWidth() / 2.0F, (float)context.guiHeight() / 2.0F + 6.0F, 0.0F);
                context.pose().scale(0.6F, 0.6F, 1.0F);
                PlayerMoodComponent moodComponent = (PlayerMoodComponent) PlayerMoodComponent.KEY.get(Minecraft.getInstance().player);
                if (moodComponent.isLowerThanMid() && TMMClient.isPlayerAliveAndInSurvival()) {
                    // Text name = Text.literal("50% sanity required to use ability");
                    Component name = Component.translatable("hud.coroner.sanity_requirements");
                    context.drawString(renderer, name, -renderer.width(name) / 2, 32, CommonColors.YELLOW);
                    return;
                }
                BodyDeathReasonComponent bodyDeathReasonComponent = (BodyDeathReasonComponent) BodyDeathReasonComponent.KEY.get(NoellesrolesClient.targetBody);
                // Text name = Text.literal("Died " + NoellesrolesClient.targetBody.age/20 + "s ago to ").append(Text.translatable("death_reason." + bodyDeathReasonComponent.deathReason.getNamespace()+ "." + bodyDeathReasonComponent.deathReason.getPath()));

                Component name = Component.translatable("hud.coroner.death_info", NoellesrolesClient.targetBody.tickCount/20).append(Component.translatable("death_reason." + bodyDeathReasonComponent.deathReason.getNamespace()+ "." + bodyDeathReasonComponent.deathReason.getPath()));
                if (bodyDeathReasonComponent.vultured) {
                    name = Component.literal("aa aaaaaa aaa aa a aaaaa aaa").withStyle(ChatFormatting.OBFUSCATED);
                }
                context.drawString(renderer, name, -renderer.width(name) / 2, 32, CommonColors.RED);
                Role foundRole = TMMRoles.CIVILIAN;
                for (Role role : TMMRoles.ROLES) {
                    if (role.identifier().equals(bodyDeathReasonComponent.playerRole)) foundRole =role;
                }
                if ((TMMClient.isPlayerSpectatingOrCreative() || gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.CORONER)) && !bodyDeathReasonComponent.vultured ) {
                    Component roleInfo = Component.translatable("hud.coroner.role_info").withColor(CommonColors.RED).append(Component.translatable("announcement.role." + bodyDeathReasonComponent.playerRole.getPath()).withColor(foundRole.color()));
                    context.drawString(renderer, roleInfo, -renderer.width(roleInfo) / 2, 48, CommonColors.WHITE);
                }
                if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.VULTURE) ) {
                    if (bodyDeathReasonComponent.vultured) {
                        Component roleInfo = Component.translatable("hud.vulture.already_consumed").withColor(ModRoles.VULTURE.color());
                        context.drawString(renderer, roleInfo, -renderer.width(roleInfo) / 2, 48, CommonColors.WHITE);
                    } else {
                        AbilityPlayerComponent abilityPlayerComponent = AbilityPlayerComponent.KEY.get(player);
                        if (abilityPlayerComponent.cooldown <= 0 && TMMClient.isPlayerAliveAndInSurvival()) {
                            Component roleInfo = Component.translatable("hud.vulture.eat", NoellesrolesClient.abilityBind.getTranslatedKeyMessage()).withColor(CommonColors.RED);
                            context.drawString(renderer, roleInfo, -renderer.width(roleInfo) / 2, 48, CommonColors.WHITE);
                        }
                    }
                }

                context.pose().popPose();
                return;
            }
        }
    }
    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Ldev/doctor4t/trainmurdermystery/game/GameFunctions;isPlayerSpectatingOrCreative(Lnet/minecraft/world/entity/player/Player;)Z"))
    private static void customRaycast(Font renderer, LocalPlayer player, GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        float range = GameFunctions.isPlayerSpectatingOrCreative(player) ? 8.0F : 2.0F;
        HitResult line = ProjectileUtil.getHitResultOnViewVector(player, (entity) -> entity instanceof PlayerBodyEntity, (double)range);
        NoellesrolesClient.targetBody = null;
        if (line instanceof EntityHitResult ehr) {
            if (ehr.getEntity() instanceof PlayerBodyEntity playerBodyEntity) {
                NoellesrolesClient.targetBody = playerBodyEntity;
            }
        }
    }
}
