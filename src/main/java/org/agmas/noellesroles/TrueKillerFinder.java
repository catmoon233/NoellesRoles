package org.agmas.noellesroles;

import org.agmas.noellesroles.component.ConspiratorPlayerComponent;
import org.agmas.noellesroles.mixin.accessor.PlayerPoisonComponentAccessor;
import org.agmas.noellesroles.role.ModRoles;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerPoisonComponent;
import dev.doctor4t.trainmurdermystery.event.EarlyKillPlayer;
import net.minecraft.server.level.ServerPlayer;

public class TrueKillerFinder {

    public static void registerEvents() {
        EarlyKillPlayer.FIND_KILLER_EVENT.register((victim, originalKiller, deathReason) -> {
            if (originalKiller != null)
                return null;
            if (!(victim instanceof ServerPlayer serverVictim))
                return null;
            Noellesroles.LOGGER.info("!!!");
            var gameWorldComponent = GameWorldComponent.KEY.get(victim.level());
            var poisonerC = PlayerPoisonComponent.KEY.maybeGet(victim).orElse(null);
            if (poisonerC != null) {
                if (poisonerC.poisoner != null) {
                    return serverVictim.level().getPlayerByUUID(poisonerC.poisoner);
                }
            }
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
