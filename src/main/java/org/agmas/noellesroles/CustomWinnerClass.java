package org.agmas.noellesroles;

import org.agmas.noellesroles.role.ModRoles;

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
            if (winStatus.equals(WinStatus.TIME) || winStatus.equals(WinStatus.PASSENGERS) || winStatus.equals(WinStatus.LOOSE_END)) {
                var players = serverLevel.players();
                var gameComponent = GameWorldComponent.KEY.get(serverLevel);
                for (var player : players) {
                    if (GameFunctions.isPlayerAliveAndSurvival(player))
                        if (gameComponent.isRole(player, ModRoles.NIAN_SHOU)) {
                            return WinStatus.NIAN_SHOU;
                        }
                }
            }
            if (winStatus.equals(WinStatus.LOOSE_END)) {
                var players = serverLevel.players();
                var gameComponent = GameWorldComponent.KEY.get(serverLevel);
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
