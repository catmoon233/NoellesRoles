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
import net.minecraft.world.effect.MobEffects;

import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.component.PlayerVolumeComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.commander.CommanderHandler;

public class NoellesrolesVoiceChatPlugin implements VoicechatPlugin {
    @Override
    public String getPluginId() {
        return Noellesroles.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        VoicechatPlugin.super.initialize(api);
    }

    public boolean vtMode(MicrophonePacketEvent event) {
        // VoicechatServerApi api = event.getVoicechat();
        var sconnection = event.getSenderConnection();
        var connection = event.getReceiverConnection();
        if (connection == null)
            return false;
        if (connection != null && connection.isInstalled() && connection.isConnected()) {
            var vcplayer = connection.getPlayer();
            if (vcplayer != null) {
                var vctplayer = vcplayer.getPlayer();
                if (vctplayer != null) {
                    ServerPlayer reciever = (ServerPlayer) vctplayer;
                    if (reciever != null) {
                        var pvc = PlayerVolumeComponent.KEY.get(reciever);
                        if (pvc.vtMode) {
                            if (sconnection != null && sconnection.isInstalled() && sconnection.isConnected()) {
                                var svcplayer = sconnection.getPlayer();
                                if (svcplayer != null) {
                                    var svctplayer = svcplayer.getPlayer();
                                    if (svctplayer != null) {
                                        ServerPlayer sender = (ServerPlayer) svctplayer;
                                        if (sender != null && sender.isSpectator()) {
                                            event.cancel();
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public void paranoidEvent(MicrophonePacketEvent event) {
        if(vtMode(event)) return;

        VoicechatServerApi api = event.getVoicechat();
        var connection = event.getSenderConnection();
        if (connection != null && connection.isInstalled() && connection.isConnected()) {
            var vcplayer = connection.getPlayer();
            if (vcplayer != null) {
                var vctplayer = vcplayer.getPlayer();
                if (vctplayer != null) {
                    var player = (ServerPlayer) vctplayer;
                    GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
                    if (gameWorldComponent != null) {
                        if (GameFunctions.isPlayerAliveAndSurvival(player)) {
                            if (gameWorldComponent.isRole(player, ModRoles.NOISEMAKER)) {
                                player.level().players().forEach((p) -> {
                                    if (p.getUUID() != player.getUUID()) {
                                        double rangeMultiplier = 2;
                                        if (player.getActiveEffectsMap().containsKey(MobEffects.LUCK)) {
                                            rangeMultiplier = 8;
                                        }
                                        if (player.distanceTo(p) <= api.getVoiceChatDistance() * rangeMultiplier) {
                                            VoicechatConnection con = api.getConnectionOf(p.getUUID());
                                            if (con != null && con.isInstalled() && con.isConnected()) {
                                                api.sendLocationalSoundPacketTo(con, event.getPacket()
                                                        .locationalSoundPacketBuilder()
                                                        .position(api.createPosition(p.getX(), p.getY(), p.getZ()))
                                                        .distance((float) api.getVoiceChatDistance())
                                                        .build());
                                            }
                                        }
                                    }
                                });
                            }
                            CommanderHandler.vcparanoidEvent(gameWorldComponent, player, event);
                        }
                    }
                }
            }
        }
        // ServerPlayer players = ((ServerPlayer)
        // event.getSenderConnection().getPlayer().getPlayer());

        // if (players.interactionManager.getGameMode().equals(GameMode.SPECTATOR)) {

        // }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::paranoidEvent);
        VoicechatPlugin.super.registerEvents(registration);
    }
}
