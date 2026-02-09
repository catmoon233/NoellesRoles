package org.agmas.noellesroles.mixin.roles.nianshou;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 游戏结算时的Mixin，处理年兽的胜利条件
 */
@Mixin(GameFunctions.class)
public class GameEndMixin {

    @Inject(method = "stopGame", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onGameEnd(net.minecraft.server.level.ServerLevel serverLevel, CallbackInfo ci) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(serverLevel);

        // 检查是否有存活的年兽
        for (Player player : serverLevel.players()) {
            if (gameWorld.isRole(player, ModRoles.NIAN_SHOU) && !GameFunctions.isPlayerEliminated(player)) {
                // 年兽存活，触发年兽胜利
                // 但是优先级比赌徒、记录员低，所以我们需要检查是否有赌徒或记录员触发胜利

                // 检查是否有赌徒胜利的条件
                boolean gamblerWin = false;
                // 赌徒的胜利逻辑在GamblerDeathMixin中处理，如果触发了，那里会直接调用stopGame
                // 所以这里不需要额外检查

                // 检查是否有记录员胜利的条件
                // 记录员的胜利在RecorderPlayerComponent.checkWinCondition中处理
                // 如果触发了，那里会直接调用stopGame

                // 由于赌徒和记录员在触发胜利时会直接调用stopGame
                // 而我们的mixin在stopGame的HEAD注入
                // 所以如果我们能执行到这里，说明赌徒和记录员没有触发胜利
                // 因此我们可以设置年兽胜利

                // 但由于优先级问题，我们需要在GameMode的tick中检测
                // 让我们换一种方式：在检测胜利条件时添加年兽的检查
                break;
            }
        }
    }
}
