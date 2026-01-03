package org.agmas.noellesroles.voice;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;

public class NoellesrolesVoiceChatPlugin implements VoicechatPlugin {
    @Override
    public String getPluginId() {
        return Noellesroles.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        VoicechatPlugin.super.initialize(api);
    }

    public void paranoidEvent(MicrophonePacketEvent event) {
        VoicechatServerApi api = event.getVoicechat();
        ServerPlayer players = ((ServerPlayer)event.getSenderConnection().getPlayer().getPlayer());
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(players.level());
        //if (players.interactionManager.getGameMode().equals(GameMode.SPECTATOR)) {
            players.level().players().forEach((p) -> {
                if (p!=players&&gameWorldComponent.isRole(players, ModRoles.NOISEMAKER)  && GameFunctions.isPlayerAliveAndSurvival( players)) {
                    if (players.distanceTo(p) <= api.getVoiceChatDistance()*1.25) {
                        VoicechatConnection con = api.getConnectionOf(p.getUUID());
                        api.sendLocationalSoundPacketTo(con, event.getPacket().locationalSoundPacketBuilder()
                                        .position(api.createPosition(p.getX(), p.getY(), p.getZ()))
                                        .distance((float)api.getVoiceChatDistance())
                                        .build());
                    }
                }
//                if (gameWorldComponent.isRole(p, Noellesroles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES) && GameFunctions.isPlayerAliveAndSurvival(p)) {
//                    if (players.distanceTo(p) <= api.getVoiceChatDistance()) {
//                        VoicechatConnection con = api.getConnectionOf(p.getUuid());
//                        api.sendLocationalSoundPacketTo(con, event.getPacket().locationalSoundPacketBuilder()
//                                        .position(api.createPosition(p.getX(), p.getY(), p.getZ()))
//                                        .distance((float)api.getVoiceChatDistance())
//                                        .build());
//                    }
//                }
            });
       // }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::paranoidEvent);
        VoicechatPlugin.super.registerEvents(registration);
    }
}
