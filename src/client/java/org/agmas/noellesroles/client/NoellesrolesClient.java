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
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.packet.AbilityC2SPacket;
import org.agmas.noellesroles.packet.BroadcastMessageS2CPacket;
import org.agmas.noellesroles.packet.VultureEatC2SPacket;
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

        for (Role role : TMMRoles.ROLES) {
            if (role.identifier().equals(Noellesroles.MORPHLING_ID)){
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
            
//            // 添加Executioner商店入口
//            if (role.identifier().equals(Noellesroles.EXECUTIONER_ID)) {
//                role.addChild(
//                        limitedInventoryScreen -> {
//                            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
//
//                            // 检查是否是Executioner角色
//                            if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.EXECUTIONER)) {
//                                ExecutionerPlayerComponent executionerComponent = ExecutionerPlayerComponent.KEY.get(MinecraftClient.getInstance().player);
//
//                                // 检查商店是否已解锁
//                                if (executionerComponent.shopUnlocked) {
//                                    // 可以在这里添加商店相关的UI元素
//                                }
//                            }
//                        }
//                );
//            }
//
//            // 添加Framing角色商店入口
//            if (role.identifier().equals(Noellesroles.JESTER_ID) ) {
//                role.addChild(
//                        limitedInventoryScreen -> {
//                            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
//                            if (
//                                gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.JESTER)) {
//                                List<ShopEntry> entries = Noellesroles.FRAMING_ROLES_SHOP;
//                                int apart = 36;
//                                int x = limitedInventoryScreen.width / 2 - (entries.size()) * apart / 2 + 9;
//                                int shouldBeY = (limitedInventoryScreen.height - 32) / 2;
//                                int y = shouldBeY - 46;
//
//                                for(int i = 0; i < entries.size(); ++i) {
//                                    limitedInventoryScreen.addDrawableChild(new LimitedInventoryScreen.StoreItemWidget(limitedInventoryScreen, x + apart * i, y, entries.get(i), i));
//                                }
//                            }
//                        }
//                );
//            }
//            if (role.identifier().equals(Noellesroles.POISONER_ID)){
//                role.addChild(
//                        limitedInventoryScreen -> {
//                List<ShopEntry> entries = HSRConstants.POISONER_SHOP_ENTRIES;
//                int apart = 38;
//                int x = limitedInventoryScreen.width / 2 - entries.size() * apart / 2 + 9;
//                int y = limitedInventoryScreen.height - 46;
//                for(int i = 0; i < entries.size(); ++i) {
//                    limitedInventoryScreen.addDrawableChild(new LimitedInventoryScreen.StoreItemWidget(limitedInventoryScreen, x + apart * i, y, (ShopEntry)entries.get(i), i));
//                }
//            });
//            }
//            if (role.identifier().equals(Noellesroles.BANDIT_ID)){
//                role.addChild(
//                        limitedInventoryScreen -> {
//                List<ShopEntry> entries = HSRConstants.BANDIT_SHOP_ENTRIES;
//                int apart = 38;
//                int x = limitedInventoryScreen.width / 2 - entries.size() * apart / 2 + 9;
//                int y = limitedInventoryScreen.height - 46;
//                for(int i = 0; i < entries.size(); ++i) {
//                    limitedInventoryScreen.addDrawableChild(new LimitedInventoryScreen.StoreItemWidget(limitedInventoryScreen, x + apart * i, y, (ShopEntry)entries.get(i), i));
//                }
//            });
//            }
//
//            // 添加Bartender角色商店入口
//            if (role.identifier().equals(Noellesroles.BARTENDER_ID)) {
//                role.addChild(
//                        limitedInventoryScreen -> {
//                            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
//                            if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.BARTENDER)) {
//
//                                int apart = 36;
//                                int x = limitedInventoryScreen.width / 2 - (entries.size()) * apart / 2 + 9;
//                                int shouldBeY = (limitedInventoryScreen.height - 32) / 2;
//                                int y = shouldBeY - 46;
//
//                                for(int i = 0; i < entries.size(); ++i) {
//                                    limitedInventoryScreen.addDrawableChild(new LimitedInventoryScreen.StoreItemWidget(limitedInventoryScreen, x + apart * i, y, entries.get(i), i));
//                                }
//                            }
//                        }
//                );
//            }
//
//            // 添加Noisemaker角色商店入口
//            if (role.identifier().equals(Noellesroles.NOISEMAKER_ID)) {
//                role.addChild(
//                        limitedInventoryScreen -> {
//                            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
//                            if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.NOISEMAKER)) {
//                                List<ShopEntry> entries = new ArrayList<>();
//                                entries.add(new ShopEntry(TMMItems.FIRECRACKER.getDefaultStack(), 75, ShopEntry.Type.TOOL));
//
//                                int apart = 36;
//                                int x = limitedInventoryScreen.width / 2 - (entries.size()) * apart / 2 + 9;
//                                int shouldBeY = (limitedInventoryScreen.height - 32) / 2;
//                                int y = shouldBeY - 46;
//
//                                for(int i = 0; i < entries.size(); ++i) {
//                                    limitedInventoryScreen.addDrawableChild(new LimitedInventoryScreen.StoreItemWidget(limitedInventoryScreen, x + apart * i, y, entries.get(i), i));
//                                }
//                            }
//                        }
//                );
//            }
        }

        Noellesroles.registerShopEntries();
        for (Role role : TMMRoles.ROLES) {
            if (role.identifier().equals(Noellesroles.THIEF_ID)) {
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
                    if (MinecraftClient.getInstance().player == null) return;
                    GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
                    if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.VULTURE)) {
                        if (targetBody == null) return;
                        ClientPlayNetworking.send(new VultureEatC2SPacket(targetBody.getUuid()));
                        return;
                    } else if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.BROADCASTER)) {
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