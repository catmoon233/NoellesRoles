package org.agmas.noellesroles.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
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
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.screen.BroadcasterInputScreen;
import org.agmas.noellesroles.client.screen.TelegrapherScreen;
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
    public static KeyMapping abilityBind;
    public static Player target;
    public static PlayerBodyEntity targetBody;
    public static Player targetFakeBody;

    public static Map<UUID, UUID> SHUFFLED_PLAYER_ENTRIES_CACHE = Maps.newHashMap();
    public static String currentBroadcastMessage = null;
    public static int broadcastMessageTicks = 0;

    @Override
    public void onInitializeClient() {

        for (Role role : TMMRoles.ROLES) {
            // if (role.identifier().equals(ModRoles.MORPHLING_ID)) {
            // role.addChild(
            // limitedInventoryScreen -> {
            // List<AbstractClientPlayer> entries = Minecraft.getInstance().level.players();
            // entries.removeIf((e) ->
            // e.getUUID().equals(Minecraft.getInstance().player.getUUID()));
            // int apart = 36;
            // int x = limitedInventoryScreen.width / 2 - (entries.size()) * apart / 2 + 9;
            // int shouldBeY = (limitedInventoryScreen.height - 32) / 2;
            // int y = shouldBeY + 80;
            //
            // for (int i = 0; i < entries.size(); ++i) {
            // MorphlingPlayerWidget child = new
            // MorphlingPlayerWidget(limitedInventoryScreen,
            // x + apart * i, y, entries.get(i), i);
            // limitedInventoryScreen.addRenderableWidget(child);
            // }
            //
            // });
            // }
            //
            // if (role.identifier().equals(ModRoles.THIEF_ID)) {
            // role.addChild(limitedInventoryScreen -> {
            // List<ShopEntry> entries = new ArrayList<>();
            // entries.add(new ShopEntry(TMMItems.BLACKOUT.getDefaultInstance(), 100,
            // ShopEntry.Type.TOOL));
            // entries.add(new ShopEntry(ModItems.MASTER_KEY.getDefaultInstance(), 200,
            // ShopEntry.Type.TOOL));
            // entries.add(new ShopEntry(ModItems.FAKE_KNIFE.getDefaultInstance(), 1000,
            // ShopEntry.Type.WEAPON));
            // int apart = 36;
            // int x = limitedInventoryScreen.width / 2 - entries.size() * apart / 2 + 9;
            // int y = (limitedInventoryScreen.height - 32) / 2 - 46;
            //
            // for (int i = 0; i < entries.size(); ++i) {
            // limitedInventoryScreen.addRenderableWidget(new
            // LimitedInventoryScreen.StoreItemWidget(
            // limitedInventoryScreen, x + apart * i, y, entries.get(i), i));
            // }
            // });
            // break;
            // }
        }

        abilityBind = KeyBindingHelper.registerKeyBinding(new KeyMapping("key." + Noellesroles.MOD_ID + ".ability",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.trainmurdermystery.keybinds"));

        ClientPlayNetworking.registerGlobalReceiver(BroadcastMessageS2CPacket.ID, (payload, context) -> {
            final var client = context.client();
            client.execute(() -> {
                if (client.player != null) {
                    if (!isPlayerInAdventureMode(client.player))
                        return;

                    currentBroadcastMessage = payload.message();
                    broadcastMessageTicks = GameConstants.getInTicks(0,
                            NoellesRolesConfig.HANDLER.instance().broadcasterMessageDuration);
                }
            });
        });
        Listen.registerEvents();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isPlayerInAdventureMode(client.player))
                return;
            insanityTime++;
            if (broadcastMessageTicks > 0) {
                broadcastMessageTicks--;
                if (broadcastMessageTicks <= 0) {
                    currentBroadcastMessage = null;
                }
            }
            if (insanityTime >= 20 * 6) {
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

            handleStalkerContinuousInput(client);

            if (abilityBind.consumeClick()) {

                FriendlyByteBuf data = PacketByteBufs.create();
                client.execute(() -> {
                    // 慕恋者持续按键检测（窥视）
                    handleAdmirerContinuousInput(client);
                    if (Minecraft.getInstance().player == null)
                        return;

                    GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                            .get(Minecraft.getInstance().player.level());

                    // 优先处理炸弹客，避免被 onAbilityKeyPressed 干扰
                    if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.BOMBER)) {
                        ClientPlayNetworking.send(new AbilityC2SPacket());
                        return;
                    }

                    // while (abilityBind.wasPressed()) {
                    onAbilityKeyPressed(client);
                    // }

                    if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.VULTURE)) {
                        if (targetBody == null)
                            return;
                        ClientPlayNetworking.send(new VultureEatC2SPacket(targetBody.getUUID()));
                        return;
                    } else if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.BROADCASTER)) {
                        if (!isPlayerInAdventureMode(client.player))
                            return;
                        client.setScreen(new TelegrapherScreen(client.screen));
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
        // registerKeyBindings();

        // 2. 注册客户端事件
        registerClientEvents();

        // 3. 注册物品提示（如果有自定义物品）
        // registerItemTooltips();

        // 4. 设置物品回调
        setupItemCallbacks();

        // 注册炸弹可见性属性
        net.minecraft.client.renderer.item.ItemProperties.register(ModItems.BOMB, Noellesroles.id("visible"),
                (stack, world, entity, seed) -> {
                    // 如果持有者是炸弹客，始终可见
                    if (entity instanceof Player player) {
                        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
                        if (gameWorldComponent.isRole(player, ModRoles.BOMBER)) {
                            return 1.0F;
                        }
                    }

                    net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(
                            net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                            net.minecraft.world.item.component.CustomData.EMPTY);
                    // 非炸弹客始终不可见
                    return 0.0F;
                });

        // 5. 注册实体渲染器
        registerEntityRenderers();

        // 6. 注册Screen
        registerScreens();
    }

    public void tooltipHelper(Item item, ItemStack itemStack, List<Component> list) {
        if (itemStack.is(item)) {
            list.addAll(
                    TextUtils.getTooltipForItem(item, Style.EMPTY.withColor(TMMItemTooltips.REGULAR_TOOLTIP_COLOR)));
        }
    }

    private boolean isPlayerInAdventureMode(AbstractClientPlayer targetPlayer) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            PlayerInfo entry = client.player.connection.getPlayerInfo(targetPlayer.getUUID());
            return entry != null && entry.getGameMode() == GameType.ADVENTURE;
        }
        return false;
    }
}