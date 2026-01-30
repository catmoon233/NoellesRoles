package org.agmas.noellesroles.mixin.roles.patroller;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.PatrollerPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public class PatrollerKillMixin {

    @Inject(method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V", at = @At("HEAD"))
    private static void onKillPlayer(Player victim, boolean spawnBody, Player killer, ResourceLocation deathReason, CallbackInfo ci) {
        if (victim == null || victim.level().isClientSide()) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(victim.level());
        if (gameWorld == null || !gameWorld.isRunning()) return;

        // 遍历所有玩家，查找附近的巡警
        for (Player player : victim.level().players()) {
            // 排除受害者自己（虽然巡警死了也不能触发能力，但以防万一）
            if (player.equals(victim)) continue;

            // 检查是否存活
            if (!GameFunctions.isPlayerAliveAndSurvival(player)) continue;

            // 检查是否是巡警
            if (!gameWorld.isRole(player, ModRoles.PATROLLER)) continue;

            // 检查距离（30格内）
            if (player.distanceToSqr(victim) > 30 * 30) continue;

            // 检查视野（参考恋慕者，这里简单检查是否有视线阻挡，或者直接用距离）
            // 题目要求"视野30格内"，通常意味着距离+视线。
            // 但考虑到Minecraft的视线检测比较复杂（raycast），且"参考恋慕者"可能指Admirer的机制。
            // AdmirerPlayerComponent中有isBoundTargetVisible()方法使用了raycast。
            // 这里我们简单使用canSee()方法，或者如果需要严格按照Admirer的逻辑，可以使用类似的raycast。
            // 为了简化且高效，先使用canSee检查。
            if (player.hasLineOfSight(victim)) {
                PatrollerPlayerComponent patrollerComponent = ModComponents.PATROLLER.get(player);
                patrollerComponent.onNearbyDeath();
            }
        }
    }
}