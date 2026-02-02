// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package org.agmas.noellesroles.item;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.TMMConfig;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.particle.HandParticle;
import dev.doctor4t.trainmurdermystery.client.render.TMMRenderLayers;
import dev.doctor4t.trainmurdermystery.compat.CrosshairaddonsCompat;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.GunShootPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import org.agmas.noellesroles.ModItems;
import org.jetbrains.annotations.NotNull;

public class PatrollerRevolverItem extends Item {
    public PatrollerRevolverItem(Item.Properties settings) {
        super(settings.durability(4));
    }

    public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        GameWorldComponent gameComponent;
        Role role;
        if (world.isClientSide) {
            gameComponent = TMMClient.gameComponent;
            if (gameComponent != null) {
                role = gameComponent.getRole(user);
                if (role != null && !role.onUseGun(user)) {
                    return InteractionResultHolder.fail(stack);
                }
            }

            HitResult collision = getGunTarget(user);
            if (collision instanceof EntityHitResult) {
                EntityHitResult entityHitResult = (EntityHitResult) collision;
                Entity target = entityHitResult.getEntity();
                ClientPlayNetworking.send(new GunShootPayload(target.getId()));
                CrosshairaddonsCompat.arrowHit();
            } else {
                ClientPlayNetworking.send(new GunShootPayload(-1));
            }

            user.setXRot(user.getXRot() - 4.0F);
            spawnHandParticle();
            user.getCooldowns().addCooldown(TMMItems.REVOLVER, 5 * 20);
        } else {
            gameComponent = (GameWorldComponent) GameWorldComponent.KEY.get(world);
            role = gameComponent.getRole(user);
            if (role != null && !role.onUseGun(user)) {
                return InteractionResultHolder.fail(stack);
            }
            user.getCooldowns().addCooldown(TMMItems.REVOLVER, 5 * 20);
            user.getCooldowns().addCooldown(ModItems.PATROLLER_REVOLVER, TMMConfig.revolverCooldown * 20);
        }

        return InteractionResultHolder.consume(stack);
    }

    public static void spawnHandParticle() {
        HandParticle handParticle = (new HandParticle()).setTexture(TMM.id("textures/particle/gunshot.png"))
                .setPos(0.1F, 0.275F, -0.2F).setMaxAge(3.0F).setSize(0.5F).setVelocity(0.0F, 0.0F, 0.0F)
                .setLight(15, 15).setAlpha(new float[] { 1.0F, 0.1F }).setRenderLayer(TMMRenderLayers::additive);
        TMMClient.handParticleManager.spawn(handParticle);
    }

    public static HitResult getGunTarget(Player user) {
        return ProjectileUtil.getHitResultOnViewVector(user, (entity) -> {
            boolean var10000;
            if (entity instanceof Player player) {
                if (GameFunctions.isPlayerAliveAndSurvival(player)) {
                    var10000 = true;
                    return var10000;
                }
            }

            var10000 = false;
            return var10000;
        }, 15.0);
    }
}
