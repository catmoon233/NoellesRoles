package org.agmas.noellesroles.mixin.roles.nianshou;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.component.NianShouPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 处理年兽的技能键（发放红包）
 */
@Mixin(targets = {"dev.doctor4t.trainmurdermystery.packet.AbilityC2SPacket$Handler"}, remap = false)
public class AbilityMixin {

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private static void onAbilityKeyPressed(Object packet, ServerPlayer sender, CallbackInfo ci) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(sender.level());

        // 检查是否是年兽
        if (gameWorld.isRole(sender, ModRoles.NIAN_SHOU)) {
            ci.cancel(); // 取消默认处理

            NianShouPlayerComponent nianShouComponent = NianShouPlayerComponent.KEY.get(sender);

            // 简单实现：检查准星对准的玩家
            Player target = null;
            // 由于raycastPlayer方法不存在，使用简化逻辑
            // 获取准星对准的玩家
            double minDistance = 5.0;
            for (Player otherPlayer : sender.level().players()) {
                if (otherPlayer == sender) {
                    continue; // 不能给自己发红包
                }
                double distance = sender.distanceTo(otherPlayer);
                if (distance <= minDistance) {
                    // 检查是否在准星方向
                    net.minecraft.world.phys.Vec3 eyePos = sender.getEyePosition();
                    net.minecraft.world.phys.Vec3 lookVec = sender.getLookAngle().normalize();
                    net.minecraft.world.phys.Vec3 toTarget = otherPlayer.position().subtract(eyePos).normalize();
                    double dotProduct = lookVec.dot(toTarget);
                    if (dotProduct > 0.8) { // 准星方向大致对准目标
                        if (target == null || distance < sender.distanceTo(target)) {
                            target = otherPlayer;
                        }
                    }
                }
            }

            if (target == null) {
                sender.displayClientMessage(
                    Component.translatable("message.noellesroles.nianshou.no_target")
                        .withStyle(ChatFormatting.RED),
                    true);
                return;
            }

            if (nianShouComponent.getRedPacketCount() <= 0) {
                sender.displayClientMessage(
                    Component.translatable("message.noellesroles.nianshou.no_red_packet")
                        .withStyle(ChatFormatting.RED),
                    true);
                return;
            }

            // 发放红包
            nianShouComponent.useRedPacket();

            // 目标玩家获得100金币
            if (target instanceof ServerPlayer targetSP) {
                dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent targetShop = dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent.KEY.get(target);
                targetShop.addToBalance(100);

                // 提示年兽
                sender.displayClientMessage(
                    Component.translatable("message.noellesroles.nianshou.red_packet_sent", target.getName())
                        .withStyle(ChatFormatting.GOLD),
                    true);

                targetSP.displayClientMessage(
                    Component.translatable("message.noellesroles.nianshou.red_packet_received", 100)
                        .withStyle(ChatFormatting.GOLD),
                    true);
            }
        }
    }
}
