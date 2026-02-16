package org.agmas.noellesroles.roles.conspirator;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.event.OnGiveKillerBalance;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import org.agmas.noellesroles.role.ModRoles;

public class ConspiratorKilledPlayer {
    public static void registerEvents() {
        OnGiveKillerBalance.EVENT.register((victim, killer, deathReason) -> {
            final var gameWorldComponent = GameWorldComponent.KEY.get(victim.level());
            if (gameWorldComponent.isRole(killer, ModRoles.CONSPIRATOR)) {
                if ("heart_attack".equals(deathReason.getPath())) {
                    return -GameConstants.getMoneyPerKill();
                }
            }
            return 0;
        });

    }
}
