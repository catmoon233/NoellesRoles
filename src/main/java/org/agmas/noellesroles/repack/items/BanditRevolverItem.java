package org.agmas.noellesroles.repack.items;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.particle.HandParticle;
import dev.doctor4t.trainmurdermystery.client.render.TMMRenderLayers;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.agmas.noellesroles.repack.BanditRevolverShootPayload;
import org.agmas.noellesroles.repack.HSRConstants;
import org.agmas.noellesroles.repack.HSRItems;
import org.jetbrains.annotations.NotNull;

import static dev.doctor4t.trainmurdermystery.item.RevolverItem.spawnHandParticle;

public class BanditRevolverItem extends Item {
    public double dropChance = (double)0.0F;

    public BanditRevolverItem(Item.Settings settings) {
        super(settings);
    }

    public TypedActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity user, Hand hand) {
            if (!user.isCreative()) {
                user.getItemCooldownManager().set(HSRItems.BANDIT_REVOLVER, 20);
            }

            HitResult collision = getGunTarget(user);
            if (collision instanceof EntityHitResult) {
                EntityHitResult entityHitResult = (EntityHitResult)collision;
                Entity target = entityHitResult.getEntity();
                this.dropChance += 0.2;

                if (user instanceof ServerPlayerEntity serverPlayer) {
                    BanditRevolverShootPayload.extracted(serverPlayer, target);
                }
            } else {
                //BanditRevolverShootPayload.extracted(serverPlayer, target);
            }

            user.setPitch(user.getPitch() - 4.0F);
            spawnHandParticle();


        return TypedActionResult.consume(user.getStackInHand(hand));
    }

//    public static void spawnHandParticle() {
////        HandParticle handParticle = (new HandParticle()).setTexture(TMM.id("textures/particle/gunshot.png")).setPos(0.1F, 0.275F, -0.2F).setMaxAge(3.0F).setSize(0.5F).setVelocity(0.0F, 0.0F, 0.0F).setLight(15, 15).setAlpha(new float[]{1.0F, 0.1F}).setRenderLayer(TMMRenderLayers::additive);
//     //   TMMClient.handParticleManager.spawn(handParticle);
//    }

    public static HitResult getGunTarget(PlayerEntity user) {
        return ProjectileUtil.getCollision(user, (entity) -> {
            boolean var10000;
            if (entity instanceof PlayerEntity player) {
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
