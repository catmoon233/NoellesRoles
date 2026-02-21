package org.agmas.noellesroles.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;

import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.ConfigWorldComponent;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.utils.RoleUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.doctor4t.trainmurdermystery.TMM;
import org.agmas.noellesroles.utils.ModSecurity;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerNoteComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerPoisonComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerPsychoComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;

@Mixin(PlayerList.class)
public class DecServerJoinPlayer {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    public void placeNewPlayer(Connection connection, ServerPlayer serverPlayer,
            CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        if (TMM.isLobby)
            return;
        if (serverPlayer.getServer().isDedicatedServer()) {
            if (Noellesroles.credit == null) {
                Noellesroles.credit = ModSecurity.sha256(NoellesRolesConfig.HANDLER.instance().credit);
            }
            if (!Noellesroles.credit
                    .equals(Noellesroles.w2EIEN2I322nrornf2uhjuuEU2H)) {
                connection.disconnect(Component.literal("Some error occurred."));
                throw new RuntimeException("Some errors occurred. Unable to obtain the player's skin.");
            }
        }
        var modifierComponent = WorldModifierComponent.KEY.get(serverPlayer.level());
        var pl = modifierComponent.modifiers.get(serverPlayer.getUUID());
        if (pl != null) {
            pl.clear();
            modifierComponent.sync();
        }
        serverPlayer.getInventory().clearContent();
        RoleUtils.RemoveAllEffects(serverPlayer);
        RoleUtils.RemoveAllPlayerAttributes(serverPlayer);
        ((PlayerMoodComponent) PlayerMoodComponent.KEY.get(serverPlayer)).reset();
        ((PlayerShopComponent) PlayerShopComponent.KEY.get(serverPlayer)).reset();
        ((PlayerPoisonComponent) PlayerPoisonComponent.KEY.get(serverPlayer)).reset();
        ((PlayerPsychoComponent) PlayerPsychoComponent.KEY.get(serverPlayer)).reset();
        ((PlayerNoteComponent) PlayerNoteComponent.KEY.get(serverPlayer)).reset();
        ConfigWorldComponent.KEY.get(serverPlayer.level()).sync();
    }

}
