package org.agmas.noellesroles;

import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.thief.ThiefPlayerComponent;
import org.agmas.noellesroles.utils.RoleUtils;

import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.event.AllowGameEnd;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.GameFunctions.WinStatus;

public class CustomWinnerClass {

    public static void registerCustomWinners() {
        AllowGameEnd.EVENT.register((serverLevel, winStatus, isLooseEnd) -> {
            if (isLooseEnd) {
                return WinStatus.NOT_MODIFY;
            }

            var gameComponent = GameWorldComponent.KEY.get(serverLevel);

            // 检查是否有小偷存活
            boolean hasThiefAlive = false;
            for (var player : serverLevel.players()) {
                if (GameFunctions.isPlayerAliveAndSurvival(player) &&
                    gameComponent.isRole(player, ModRoles.THIEF)) {
                    hasThiefAlive = true;
                    break;
                }
            }

            // 如果有小偷存活，检查小偷独立胜利条件
            if (hasThiefAlive) {
                // 检查小偷是否满足独立胜利条件
                if (ThiefPlayerComponent.checkThiefVictory(serverLevel)) {
                    return WinStatus.CUSTOM;
                }

                // 如果小偷存活且游戏要结束（乘客或杀手胜利），阻止游戏结束
                if (winStatus.equals(WinStatus.PASSENGERS) ||
                    winStatus.equals(WinStatus.KILLERS)) {
                    return WinStatus.NONE; // 游戏继续
                }
            }

            if (winStatus.equals(WinStatus.TIME) || winStatus.equals(WinStatus.PASSENGERS) || winStatus.equals(WinStatus.LOOSE_END)) {
                var players = serverLevel.players();
                for (var player : players) {
                    if (GameFunctions.isPlayerAliveAndSurvival(player))
                        if (gameComponent.isRole(player, ModRoles.NIAN_SHOU)) {
                            // 年兽存活时，使用 RoleUtils.customWinnerWin 设置 CustomWinnerID
                            RoleUtils.customWinnerWin(serverLevel, WinStatus.NIAN_SHOU, "nianshou", null);
                            return WinStatus.NIAN_SHOU;
                        }
                }
            }
            if (winStatus.equals(WinStatus.LOOSE_END)) {
                var players = serverLevel.players();
                for (var player : players) {
                    if (GameFunctions.isPlayerAliveAndSurvival(player))
                        if (gameComponent.isRole(player, TMMRoles.LOOSE_END)) {
                            return WinStatus.LOOSE_END;
                        }
                }
                return WinStatus.PASSENGERS;
            }
            return WinStatus.NOT_MODIFY;
        });
    }
}
