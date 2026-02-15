package org.agmas.noellesroles.mixin.roles.avengerKill;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import pro.fazeclan.river.stupid_express.modifier.refugee.cca.RefugeeComponent;

import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.component.AvengerPlayerComponent;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入到 GameFunctions.killPlayer 方法
 * 用于捕获凶手信息并激活复仇者能力
 */
@Mixin(GameFunctions.class)
public abstract class AvengerKillMixin {

    /**
     * 在玩家被杀死时触发
     * 检查是否有复仇者绑定了受害者，如果有则激活其能力
     */
    @Inject(method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V", at = @At("HEAD"))
    private static void onKillPlayer(Player victim, boolean spawnBody, Player killer, ResourceLocation deathReason,
            CallbackInfo ci) {
        if (victim == null)
            return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(victim.level());
        if (gameWorld == null)
            return;
        var refugeeC = RefugeeComponent.KEY.get(victim.level());
        boolean isRefugeeAlive = false;
        if (refugeeC.isAnyRevivals) {
            isRefugeeAlive = true;
        }
        if (isRefugeeAlive)
            return;
        // 遍历所有玩家，检查是否有复仇者绑定了这个受害者
        for (Player player : victim.level().players()) {
            if (!gameWorld.isRole(player, ModRoles.AVENGER))
                continue;
            if (player.equals(victim))
                continue; // 复仇者自己死亡不触发

            AvengerPlayerComponent avengerComponent = ModComponents.AVENGER.get(player);

            // 检查这个复仇者是否绑定了受害者
            if (avengerComponent.targetPlayer != null &&
                    avengerComponent.targetPlayer.equals(victim.getUUID()) &&
                    !avengerComponent.activated) {

                // 激活复仇者能力，传入凶手信息
                if (killer != null) {
                    avengerComponent.activate(killer.getUUID());
                    avengerComponent.targetName = killer.getName().getString();
                } else {
                    avengerComponent.activate(null);
                }

                String playerName = player.getName().getString();
                String victimName = victim.getName().getString();
                String killerName = killer != null ? killer.getName().getString() : "未知";

                player.displayClientMessage(Component.literal("你绑定的目标 " + victimName + " 被 " + killerName + " 杀死了"),
                        true);
                Noellesroles.LOGGER.info("复仇者 {} 绑定的目标 {} 被 {} 杀死，激活复仇者能力", playerName, victimName, killerName);
            }
        }
    }
}