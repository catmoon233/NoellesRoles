package org.agmas.noellesroles.repack.items;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.particle.HandParticle;
import dev.doctor4t.trainmurdermystery.client.render.TMMRenderLayers;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;

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
import org.agmas.noellesroles.repack.BanditRevolverShootPayload;
import org.agmas.noellesroles.repack.HSRConstants;
import org.agmas.noellesroles.repack.HSRItems;
import org.jetbrains.annotations.NotNull;

import static dev.doctor4t.trainmurdermystery.item.RevolverItem.spawnHandParticle;

public class BanditRevolverItem extends Item {
    public double dropChance = (double)0.0F;

    public BanditRevolverItem(Item.Properties settings) {
        super(settings);
    }

    public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player user, InteractionHand hand) {
        if (!user.isCreative()) {
            user.getCooldowns().addCooldown(HSRItems.BANDIT_REVOLVER, 20*12);
        }
        if (world.isClientSide) {
            final var gameComponent = TMMClient.gameComponent;
            if (gameComponent != null) {
                final var role = gameComponent.getRole(user);
                if (role != null) {
                    if (!role.onUseGun(user)) {
                        return InteractionResultHolder.fail(user.getItemInHand(hand));
                    }
                }
            }
            user.setXRot(user.getXRot() - 4.0F);
            spawnHandParticle();

            HitResult collision = getGunTarget(user);
            if (collision instanceof EntityHitResult) {
                EntityHitResult entityHitResult = (EntityHitResult) collision;
                Entity target = entityHitResult.getEntity();
                this.dropChance += 0.3;
                ClientPlayNetworking.send(new BanditRevolverShootPayload(target.getId()));
            } else {
                
                ClientPlayNetworking.send(new BanditRevolverShootPayload(-1));
            }
        }else{
            final var gameComponent = TMMClient.gameComponent;
            if (gameComponent != null) {
                final var role = gameComponent.getRole(user);
                if (role != null) {
                    if (!role.onUseGun(user)) {
                        return InteractionResultHolder.fail(user.getItemInHand(hand));
                    }
                }
            }
        }
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }

//    public static void spawnHandParticle() {
////        HandParticle handParticle = (new HandParticle()).setTexture(TMM.id("textures/particle/gunshot.png")).setPos(0.1F, 0.275F, -0.2F).setMaxAge(3.0F).setSize(0.5F).setVelocity(0.0F, 0.0F, 0.0F).setLight(15, 15).setAlpha(new float[]{1.0F, 0.1F}).setRenderLayer(TMMRenderLayers::additive);
//     //   TMMClient.handParticleManager.spawn(handParticle);
//    }

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
        }, (double)15.0F);
    }
}
