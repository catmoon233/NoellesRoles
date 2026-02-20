package org.agmas.noellesroles.client;

import org.agmas.noellesroles.client.screen.BroadcasterScreen;
import org.agmas.noellesroles.packet.AbilityC2SPacket;
import org.agmas.noellesroles.packet.VultureEatC2SPacket;
import org.agmas.noellesroles.role.ModRoles;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class ClientAbilityHandler {

    public static void handler(Minecraft client) {
        // 慕恋者持续按键检测（窥视）
        RicesRoleRhapsodyClient.handleAdmirerContinuousInput(client);
        if (Minecraft.getInstance().player == null)
            return;

        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                .get(Minecraft.getInstance().player.level());

        // 优先处理炸弹客，避免被 onAbilityKeyPressed 干扰
        if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.BOMBER)) {
            ClientPlayNetworking.send(new AbilityC2SPacket());
            return;
        }

        if (RicesRoleRhapsodyClient.onAbilityKeyPressed(client)) {
            return;
        }

        if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.VULTURE)) {
            if (NoellesrolesClient.targetBody == null)
                return;
            ClientPlayNetworking.send(new VultureEatC2SPacket(NoellesrolesClient.targetBody.getUUID()));
            return;
        } else if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.BROADCASTER)) {
            if (!NoellesrolesClient.isPlayerInAdventureMode(client.player))
                return;
            client.execute(() -> {
                client.setScreen(new BroadcasterScreen());
            });
            return;
        }
        ClientPlayNetworking.send(new AbilityC2SPacket());
    }

}
