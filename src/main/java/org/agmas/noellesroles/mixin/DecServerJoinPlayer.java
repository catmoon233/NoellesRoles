package org.agmas.noellesroles.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;

import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.ConfigWorldComponent;
import org.agmas.noellesroles.utils.RoleUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        var modifierComponent = WorldModifierComponent.KEY.get(serverPlayer.level());
        var pl = modifierComponent.modifiers.get(serverPlayer.getUUID());
        if (pl != null) {
            pl.clear();
            modifierComponent.sync();
        }
        RoleUtils.RemoveAllPlayerAttributes(serverPlayer);
        ((PlayerMoodComponent) PlayerMoodComponent.KEY.get(serverPlayer)).reset();
        ((PlayerShopComponent) PlayerShopComponent.KEY.get(serverPlayer)).reset();
        ((PlayerPoisonComponent) PlayerPoisonComponent.KEY.get(serverPlayer)).reset();
        ((PlayerPsychoComponent) PlayerPsychoComponent.KEY.get(serverPlayer)).reset();
        ((PlayerNoteComponent) PlayerNoteComponent.KEY.get(serverPlayer)).reset();
        // ResetPlayerEvent.EVENT.invoker().resetPlayer(serverPlayer);
        ConfigWorldComponent.KEY.get(serverPlayer.level()).sync();
        WorldModifierComponent.KEY.get(serverPlayer.level()).sync();
    }

}
