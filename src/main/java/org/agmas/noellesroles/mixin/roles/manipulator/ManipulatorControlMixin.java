package org.agmas.noellesroles.mixin.roles.manipulator;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.manipulator.ManipulatorPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class ManipulatorControlMixin {
    @Shadow public abstract boolean isLocalPlayer();

    private Player asPlayer() {
        return (Player)( Object) this;
    }

    /**
     * 拦截被操纵玩家的移动输入，当他们被操纵时，不允许他们自己移动
     */
//    @Redirect(method = "travel", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;xa:D"))
//    private double redirectMovementX(Player instance) {
//        // 检查是否有操纵者正在控制此玩家
//        if (isPlayerBeingControlled(instance)) {
//            // 如果被控制，返回0，阻止其自主移动
//            return 0.0D;
//        }
//        return instance.xa;
//    }
//
//    @Redirect(method = "travel", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;za:D"))
//    private double redirectMovementZ(Player instance) {
//        // 检查是否有操纵者正在控制此玩家
//        if (isPlayerBeingControlled(instance)) {
//            // 如果被控制，返回0，阻止其自主移动
//            return 0.0D;
//        }
//        return instance.za;
//    }
//
//    @Redirect(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;yRot:F"))
//    private float redirectYaw(Player instance) {
//        // 检查是否有操纵者正在控制此玩家
//        if (isPlayerBeingControlled(instance)) {
//            // 如果被控制，返回操纵者的yaw
//            Player controller = getPlayerController(instance);
//            if (controller != null) {
//                return controller.getYRot();
//            }
//            return instance.yRot;
//        }
//        return instance.yRot;
//    }
//
//    @Redirect(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;xRot:F"))
//    private float redirectPitch(Player instance) {
//        // 检查是否有操纵者正在控制此玩家
//        if (isPlayerBeingControlled(instance)) {
//            // 如果被控制，返回操纵者的pitch
//            Player controller = getPlayerController(instance);
//            if (controller != null) {
//                return controller.getXRot();
//            }
//            return instance.xRot;
//        }
//        return instance.xRot;
//    }

    /**
     * 阻止被控制的玩家使用物品
     */
    @Inject(method = "sweepAttack", at = @At("HEAD"), cancellable = true)
    private void preventItemUse(CallbackInfo ci) {
        Player player = asPlayer();
        if (isPlayerBeingControlled(player)) {
            ci.cancel(); // 阻止摆动手臂（这会影响物品使用）
        }
    }

    /**
     * 检查玩家是否正在被控制
     */
    private boolean isPlayerBeingControlled(Player player) {
        // 遍历所有玩家，检查是否有人正在控制当前玩家
        for (Player otherPlayer : player.level().players()) {
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(otherPlayer.level());
            if (gameWorldComponent.isRole(otherPlayer, ModRoles.MANIPULATOR)) {
                ManipulatorPlayerComponent manipulatorComponent = ManipulatorPlayerComponent.KEY.get(otherPlayer);
                if (manipulatorComponent.isControlling && 
                    manipulatorComponent.target != null && 
                    manipulatorComponent.target.equals(player.getUUID())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取控制当前玩家的操纵者
     */
    private Player getPlayerController(Player player) {
        for (Player otherPlayer : player.level().players()) {
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(otherPlayer.level());
            if (gameWorldComponent.isRole(otherPlayer, ModRoles.MANIPULATOR)) {
                ManipulatorPlayerComponent manipulatorComponent = ManipulatorPlayerComponent.KEY.get(otherPlayer);
                if (manipulatorComponent.isControlling && 
                    manipulatorComponent.target != null && 
                    manipulatorComponent.target.equals(player.getUUID())) {
                    return otherPlayer;
                }
            }
        }
        return null;
    }
}