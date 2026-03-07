package org.agmas.noellesroles.item;

// import org.agmas.noellesroles.init.ModItems;
// import org.agmas.noellesroles.packet.TryThrowKnifePacket;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.compat.CrosshairaddonsCompat;
import dev.doctor4t.trainmurdermystery.item.KnifeItem;
import dev.doctor4t.trainmurdermystery.util.KnifeStabPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrowingKnife extends KnifeItem {
    public ThrowingKnife(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (!user.isSpectator()) {
            if (remainingUseTicks < this.getUseDuration(stack, user) - 8 && user instanceof Player) {
                Player attacker = (Player) user;
                if (world.isClientSide) {
                    GameWorldComponent game = (GameWorldComponent) GameWorldComponent.KEY.get(world);
                    Role role = game.getRole(attacker);
                    if (role != null && !role.onUseKnife(attacker)) {
                        return;
                    }

                    HitResult collision = getKnifeTarget(attacker);
                    if (collision instanceof EntityHitResult) {
                        EntityHitResult entityHitResult = (EntityHitResult) collision;
                        Entity target = entityHitResult.getEntity();
                        if (TMM.REPLAY_MANAGER != null) {
                            TMM.REPLAY_MANAGER.recordItemUse(user.getUUID(), BuiltInRegistries.ITEM.getKey(this));
                        }

                        ClientPlayNetworking.send(new KnifeStabPayload(target.getId()));
                        CrosshairaddonsCompat.onAttack(target);
                    } else {
                        // 发射飞刀
                        // if (attacker.getMainHandItem().is(ModItems.THROWING_KNIFE)) {
                        //     ClientPlayNetworking.send(new TryThrowKnifePacket());
                        // }
                    }
                    return;
                }
            }

        }
    }
}
