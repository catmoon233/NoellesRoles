package org.agmas.noellesroles.mixin.roles.nianshou;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.MurderGameMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

/**
 * 修改MurderGameMode的胜利检测逻辑，添加年兽的胜利条件
 */
@Mixin(MurderGameMode.class)
public class MurderGameModeMixin {

    @Inject(method = "tickServerGameLoop", at = @At(value = "INVOKE", target = "Ldev/doctor4t/trainmurdermystery/game/GameFunctions$WinStatus;equals(Ljava/lang/Object;)Z", ordinal = 1), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void checkNianShouWinCondition(ServerLevel serverWorld, GameWorldComponent gameWorldComponent, CallbackInfo ci, GameFunctions.WinStatus winStatus) {
        // 检查是否有存活的年兽
        boolean hasNianShouAlive = false;
        for (ServerPlayer player : serverWorld.players()) {
            if (gameWorldComponent.isRole(player, ModRoles.NIAN_SHOU) && !GameFunctions.isPlayerEliminated(player)) {
                hasNianShouAlive = true;
                break;
            }
        }

        // 如果有年兽存活且游戏即将结束（超时或其他正常结束条件），触发年兽胜利
        if (hasNianShouAlive && winStatus != GameFunctions.WinStatus.NONE) {
            // 检查是否是正常的游戏结束（不是赌徒或记录员胜利）
            if (winStatus == GameFunctions.WinStatus.TIME || winStatus == GameFunctions.WinStatus.PASSENGERS || winStatus == GameFunctions.WinStatus.KILLERS) {
                // 设置年兽胜利（使用NIAN_SHOU特有的胜利状态）
                GameRoundEndComponent.KEY.get(serverWorld).setRoundEndData(serverWorld.players(), GameFunctions.WinStatus.NIAN_SHOU);
                GameFunctions.stopGame(serverWorld);

                // 广播胜利消息
                for (ServerPlayer player : serverWorld.players()) {
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("announcement.win.nianshou")
                            .withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD),
                        true);
                }
            }
        }
    }
}
