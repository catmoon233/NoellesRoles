package org.agmas.noellesroles;

import org.agmas.noellesroles.component.ConspiratorPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerPoisonComponent;
import dev.doctor4t.trainmurdermystery.event.EarlyKillPlayer;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.server.level.ServerPlayer;

public class TrueKillerFinder {

    public static void registerEvents() {
        EarlyKillPlayer.FIND_KILLER_EVENT.register((victim, originalKiller, deathReason) -> {
            if (!(victim instanceof ServerPlayer serverVictim))
                return null;
            // Noellesroles.LOGGER.info("!!!");
            var gameWorldComponent = GameWorldComponent.KEY.get(victim.level());
            var poisonerC = PlayerPoisonComponent.KEY.maybeGet(victim).orElse(null);
            if (poisonerC != null) {
                if (poisonerC.poisoner != null && poisonerC.poisonTicks >= 0) {
                    var poisonerP = serverVictim.level().getPlayerByUUID(poisonerC.poisoner);
                    if (poisonerP != null && !deathReason.getPath().equals("poison") && originalKiller != null
                            && !poisonerC.poisoner.equals(originalKiller.getUUID())) {

                        GameFunctions.killPlayer(victim, false, poisonerP, TMM.id("poison"));
                        return null;
                    }
                    if (originalKiller != null)
                        return null;
                    return poisonerP;
                }
            }

            if (originalKiller != null)
                return null;
            if (gameWorldComponent.isRole(serverVictim, ModRoles.CONSPIRATOR))
                return null;
            // 是否为阴谋家击杀
            for (var player : serverVictim.level().players()) {
                if (gameWorldComponent.isRole(player, ModRoles.CONSPIRATOR)) {
                    var consC = ConspiratorPlayerComponent.KEY.maybeGet(player).orElse(null);
                    if (consC != null) {
                        if (consC.hasBeenGuessedToDie(victim.getUUID())) {
                            return player;
                        }
                    }
                }
            }
            // 没找到
            return null;
        });
    }

}
