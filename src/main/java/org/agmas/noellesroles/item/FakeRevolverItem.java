package org.agmas.noellesroles.item;

import dev.doctor4t.trainmurdermystery.item.RevolverItem;
import dev.doctor4t.trainmurdermystery.util.GunShootPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class FakeRevolverItem extends RevolverItem {
    public FakeRevolverItem(Settings settings) {
        super(settings);
    }
    public TypedActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity user, Hand hand) {
        if (hand==Hand.OFF_HAND)return TypedActionResult.pass(user.getStackInHand(hand));
        if (user.getStackInHand(hand).getDamage() < user.getStackInHand(hand).getMaxDamage()) {
            user.getStackInHand(hand).damage(1,user, EquipmentSlot.MAINHAND);
            if (world.isClient) {
                HitResult collision = getGunTarget(user);
                if (collision instanceof EntityHitResult) {
                    EntityHitResult entityHitResult = (EntityHitResult) collision;
                    Entity target = entityHitResult.getEntity();
                    ClientPlayNetworking.send(new GunShootPayload(target.getId()));
                } else {
                    ClientPlayNetworking.send(new GunShootPayload(-1));
                }

                user.setPitch(user.getPitch() - 4.0F);
                spawnHandParticle();
            }
        }
        return TypedActionResult.consume(user.getStackInHand(hand));
    }
}
