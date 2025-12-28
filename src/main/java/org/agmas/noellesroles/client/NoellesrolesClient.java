package org.agmas.noellesroles.client;

import com.google.common.collect.Maps;
import dev.doctor4t.ratatouille.util.TextUtils;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.trainmurdermystery.client.util.TMMItemTooltips;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.screen.BroadcasterInputScreen;
import org.agmas.noellesroles.client.widget.MorphlingPlayerWidget;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.packet.AbilityC2SPacket;
import org.agmas.noellesroles.packet.BroadcastMessageS2CPacket;
import org.agmas.noellesroles.packet.VultureEatC2SPacket;

import org.agmas.noellesroles.role.ModRoles;
import org.lwjgl.glfw.GLFW;

import java.util.*;

import static org.agmas.noellesroles.client.RicesRoleRhapsodyClient.*;

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
        for (Role role : TMMRoles.ROLES) {
            if (role.identifier().equals(ModRoles.MORPHLING_ID)){
                role.addChild(
                        limitedInventoryScreen -> {
                            List<AbstractClientPlayerEntity> entries = MinecraftClient.getInstance().world.getPlayers();
                            entries.removeIf((e) -> e.getUuid().equals(MinecraftClient.getInstance().player.getUuid()));
                            int apart = 36;
                            int x = limitedInventoryScreen.width / 2 - (entries.size()) * apart / 2 + 9;
                            int shouldBeY = (limitedInventoryScreen.height - 32) / 2;
                            int y = shouldBeY + 80;

                            for(int i = 0; i < entries.size(); ++i) {
                                MorphlingPlayerWidget child = new MorphlingPlayerWidget(limitedInventoryScreen, x + apart * i, y, entries.get(i), i);
                                    limitedInventoryScreen.addDrawableChild(child);
                            }

                        }
                );
            }

            if (role.identifier().equals(ModRoles.THIEF_ID)) {
                role.addChild(limitedInventoryScreen -> {
                    List<ShopEntry> entries = new ArrayList<>();
                    entries.add(new ShopEntry(TMMItems.BLACKOUT.getDefaultStack(), 100, ShopEntry.Type.TOOL));                    entries.add(new ShopEntry(ModItems.MASTER_KEY.getDefaultStack(), 200, ShopEntry.Type.TOOL));
                    entries.add(new ShopEntry(ModItems.FAKE_KNIFE.getDefaultStack(), 1000, ShopEntry.Type.WEAPON));
                    int apart = 36;
                    int x = limitedInventoryScreen.width / 2 - entries.size() * apart / 2 + 9;
                    int y = (limitedInventoryScreen.height - 32) / 2 - 46;

                    for (int i = 0; i < entries.size(); ++i) {
                        limitedInventoryScreen.addDrawableChild(new LimitedInventoryScreen.StoreItemWidget(limitedInventoryScreen, x + apart * i, y, entries.get(i), i));
                    }
                });
                break;
            }
        }

        abilityBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + Noellesroles.MOD_ID + ".ability", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.trainmurdermystery.keybinds"));

        ClientPlayNetworking.registerGlobalReceiver(BroadcastMessageS2CPacket.ID, (payload, context) -> {
            final var client = context.client();
            client.execute(() -> {
                if (client.player != null) {
                    if (!isPlayerInAdventureMode(client.player))return;

                    currentBroadcastMessage = payload.message();
                    broadcastMessageTicks = GameConstants.getInTicks(0, NoellesRolesConfig.HANDLER.instance().broadcasterMessageDuration);
                }
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isPlayerInAdventureMode(client.player))return;
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
                    // 跟踪者持续按键检测（窥视和蓄力）
                    handleStalkerContinuousInput(client);
                    // 慕恋者持续按键检测（窥视）
                    handleAdmirerContinuousInput(client);
                    if (MinecraftClient.getInstance().player == null) return;
                    //while (abilityBind.wasPressed()) {
                    onAbilityKeyPressed(client);
                    //}

                    GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
                    if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, ModRoles.VULTURE)) {
                        if (targetBody == null) return;
                        ClientPlayNetworking.send(new VultureEatC2SPacket(targetBody.getUuid()));
                        return;
                    } else if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, ModRoles.BROADCASTER)) {
                        if (!isPlayerInAdventureMode(client.player))return;
                        client.setScreen(new BroadcasterInputScreen(client.currentScreen));
                        return;
                    }
                    ClientPlayNetworking.send(new AbilityC2SPacket());
                });

            }
        });

        ItemTooltipCallback.EVENT.register(((itemStack, tooltipContext, tooltipType, list) -> {
            tooltipHelper(ModItems.DEFENSE_VIAL, itemStack, list);
            tooltipHelper(ModItems.DELUSION_VIAL, itemStack, list);
        }));
        //registerKeyBindings();

        // 2. 注册客户端事件
        registerClientEvents();

        // 3. 注册物品提示（如果有自定义物品）
//        registerItemTooltips();

        // 4. 设置物品回调
        setupItemCallbacks();

        // 5. 注册实体渲染器
        registerEntityRenderers();

        // 6. 注册Screen
        registerScreens();
    }

    public void tooltipHelper(Item item, ItemStack itemStack, List<Text> list) {
        if (itemStack.isOf(item)) {
            list.addAll(TextUtils.getTooltipForItem(item, Style.EMPTY.withColor(TMMItemTooltips.REGULAR_TOOLTIP_COLOR)));
        }
    }

    private boolean isPlayerInAdventureMode(AbstractClientPlayerEntity targetPlayer) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            PlayerListEntry entry = client.player.networkHandler.getPlayerListEntry(targetPlayer.getUuid());
            return entry != null && entry.getGameMode() == GameMode.ADVENTURE;
        }
        return false;
    }
}