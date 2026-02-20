package org.agmas.noellesroles.roles.commander;

import org.agmas.noellesroles.commands.BroadcastCommand;
import org.agmas.noellesroles.component.NoellesRolesAbilityPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CommanderHandler {

    public static void vcparanoidEvent(GameWorldComponent gameWorldComponent, ServerPlayer player,
            MicrophonePacketEvent event) {
        var napc = NoellesRolesAbilityPlayerComponent.KEY.get(player);
        var api = event.getVoicechat();
        if (napc.status == 1) { // 给杀手广播
            event.cancel();
            player.level().players().forEach((p) -> {
                if (p.getUUID() != player.getUUID()) {
                    var role = gameWorldComponent.getRole(p.getUUID());
                    if (role == null)
                        return;
                    if (role.isNeutrals() && !role.isNeutralForKiller())
                        return;
                    if (!role.isNeutrals() && (!role.canUseKiller() || role.isInnocent()))
                        return;
                    VoicechatConnection con = api.getConnectionOf(p.getUUID());
                    if (con != null && con.isInstalled() && con.isConnected()) {
                        api.sendLocationalSoundPacketTo(con, event.getPacket()
                                .locationalSoundPacketBuilder()
                                .position(api.createPosition(p.getX(), p.getY(), p.getZ()))
                                .distance((float) api.getVoiceChatDistance())
                                .build());
                    }
                }
            });
            return;
        }
    }

    public static void registerChatEvent() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, serverPlayer, bound) -> {
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(serverPlayer.level());
            if (gameWorldComponent.isRole(serverPlayer, ModRoles.COMMANDER)) {
                var napc = NoellesRolesAbilityPlayerComponent.KEY.get(serverPlayer);
                if (napc.status == 1) { // 杀手频道
                    var broadcastMessage = Component
                            .translatable("message.commander.broadcast_prefix",
                                    Component.literal("").append(serverPlayer.getDisplayName())
                                            .withStyle(ChatFormatting.GREEN),
                                    Component.literal(message.signedContent()).withStyle(ChatFormatting.WHITE))
                            .withStyle(ChatFormatting.LIGHT_PURPLE);
                    serverPlayer.getServer().getPlayerList().getPlayers().forEach((p) -> {
                        var role = gameWorldComponent.getRole(p.getUUID());
                        if (role == null)
                            return;
                        if (!GameFunctions.isPlayerAliveAndSurvival(p)) {
                            p.displayClientMessage(message.decoratedContent(), false);
                        }
                        if (role.isNeutrals() && !role.isNeutralForKiller())
                            return;
                        if (!role.isNeutrals() && (!role.canUseKiller() || role.isInnocent()))
                            return;
                        BroadcastCommand.BroadcastMessage(p, broadcastMessage);
                        p.displayClientMessage(message.decoratedContent(), false);
                    });
                    return false;
                }
            }
            return true;
        });
    }

    public static void abilityActived() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'abilityActived'");
    }

}
