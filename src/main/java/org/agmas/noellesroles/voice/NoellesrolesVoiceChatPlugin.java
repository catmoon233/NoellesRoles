package org.agmas.noellesroles.voice;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import org.agmas.noellesroles.Noellesroles;

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
        final var player = event.getSenderConnection().getPlayer();
        if (player==null)return;
        ServerPlayerEntity players = ((ServerPlayerEntity) player.getPlayer());
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(players.getWorld());
        if (gameWorldComponent==null)return;
        //if (players.interactionManager.getGameMode().equals(GameMode.SPECTATOR)) {
            players.getWorld().getPlayers().forEach((p) -> {
                if (p!=players) {
                    if (gameWorldComponent.isRole(players, Noellesroles.NOISEMAKER) && GameFunctions.isPlayerAliveAndSurvival(p) && GameFunctions.isPlayerAliveAndSurvival(players)) {
                        if (players.distanceTo(p) <= api.getVoiceChatDistance() * 1.5) {
                            VoicechatConnection con = api.getConnectionOf(p.getUuid());
                            api.sendLocationalSoundPacketTo(con, event.getPacket().locationalSoundPacketBuilder()
                                    .position(api.createPosition(p.getX(), p.getY(), p.getZ()))
                                    .distance((float) api.getVoiceChatDistance() *1.5f)
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
                }
            });
       // }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::paranoidEvent);
        VoicechatPlugin.super.registerEvents(registration);
    }
}
