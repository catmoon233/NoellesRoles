package org.agmas.noellesroles.client;

import com.google.common.collect.Maps;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.packet.AbilityC2SPacket;
import org.agmas.noellesroles.packet.VultureEatC2SPacket;
import org.agmas.noellesroles.packet.BroadcastMessageS2CPacket;
import org.agmas.noellesroles.client.screen.BroadcasterInputScreen;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class NoellesrolesClient implements ClientModInitializer {


    public static int insanityTime = 0;
    public static KeyBinding abilityBind;
    public static PlayerEntity target;
    public static PlayerBodyEntity targetBody;

    public static Map<UUID, UUID> SHUFFLED_PLAYER_ENTRIES_CACHE = Maps.newHashMap();

    public static String currentBroadcastMessage = null;
    public static int broadcastMessageTicks = 0;

@Override
public void onInitializeClient() {
    abilityBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + Noellesroles.MOD_ID + ".ability", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.trainmurdermystery.keybinds"));

    ClientPlayNetworking.registerGlobalReceiver(BroadcastMessageS2CPacket.ID, (payload, context) -> {
        context.client().execute(() -> {
            if (context.client().player != null) {
                currentBroadcastMessage = payload.message();
                broadcastMessageTicks = GameConstants.getInTicks(0, NoellesRolesConfig.HANDLER.instance().broadcasterMessageDuration);
            }
        });
    });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            insanityTime++;
            if (broadcastMessageTicks > 0) {
                broadcastMessageTicks--;
                if (broadcastMessageTicks <= 0) {
                    currentBroadcastMessage = null;
                }
            }
            if (insanityTime >= 20*6) {
                insanityTime = 0;
                List<UUID> keys = new ArrayList<UUID>(TMMClient.PLAYER_ENTRIES_CACHE.keySet());
                List<UUID> originalkeys = new ArrayList<UUID>(TMMClient.PLAYER_ENTRIES_CACHE.keySet());
                Collections.shuffle(keys);
                int i = 0;
                for (UUID o : originalkeys) {
                    SHUFFLED_PLAYER_ENTRIES_CACHE.put(o, keys.get(i));
                    i++;
                }
            }
            if (abilityBind.wasPressed()) {
                PacketByteBuf data = PacketByteBufs.create();
                client.execute(() -> {
                    if (MinecraftClient.getInstance().player == null) return;
                    GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
                    if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.VULTURE)) {
                        if (targetBody == null) return;
                        ClientPlayNetworking.send(new VultureEatC2SPacket(targetBody.getUuid()));
                        return;
                    } else if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.BROADCASTER)) {
                        client.setScreen(new BroadcasterInputScreen(client.currentScreen));
                        return;
                    }
                    ClientPlayNetworking.send(new AbilityC2SPacket());
                });
            }
        });
    }}
