package org.agmas.noellesroles.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import dev.doctor4t.ratatouille.util.TextUtils;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.util.TMMItemTooltips;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.blood.BloodMain;
import org.agmas.noellesroles.client.screen.GuessRoleScreen;
import org.agmas.noellesroles.client.screen.LockGameScreen;
import org.agmas.noellesroles.client.screen.RoleIntroduceScreen;
import org.agmas.noellesroles.client.event.MutableComponentResult;
import org.agmas.noellesroles.client.event.OnMessageBelowMoneyRenderer;
import org.agmas.noellesroles.client.screen.BroadcasterScreen;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.entity.LockEntityManager;
import org.agmas.noellesroles.packet.AbilityC2SPacket;
import org.agmas.noellesroles.packet.BroadcastMessageS2CPacket;
import org.agmas.noellesroles.packet.OpenIntroPayload;
import org.agmas.noellesroles.packet.OpenLockGuiS2CPacket;
import org.agmas.noellesroles.packet.PlayerResetS2CPacket;
import org.agmas.noellesroles.packet.VultureEatC2SPacket;
import org.agmas.noellesroles.packet.BloodConfigS2CPacket;
import org.agmas.noellesroles.role.ModRoles;
import org.lwjgl.glfw.GLFW;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.agmas.noellesroles.client.RicesRoleRhapsodyClient.*;

public class NoellesrolesClient implements ClientModInitializer {

    public static int insanityTime = 0;
    public static KeyMapping roleGuessNoteClientBind;
    public static KeyMapping abilityBind;
    public static KeyMapping roleIntroClientBind;
    public static Player target;
    public static PlayerBodyEntity targetBody;
    public static Player targetFakeBody;

    public static Map<UUID, UUID> SHUFFLED_PLAYER_ENTRIES_CACHE = Maps.newHashMap();
    public static Component currentBroadcastMessage = null;
    public static int broadcastMessageTicks = 0;
    public static BloodMain bloodMain = new BloodMain();

    @Override
    public void onInitializeClient() {

        roleIntroClientBind = KeyBindingHelper
                .registerKeyBinding(new KeyMapping("key." + Noellesroles.MOD_ID + ".role_intro",
                        InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, "category.trainmurdermystery.keybinds"));
        roleGuessNoteClientBind = KeyBindingHelper
                .registerKeyBinding(new KeyMapping("key." + Noellesroles.MOD_ID + ".guess_role_note",
                        InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, "category.trainmurdermystery.keybinds"));
        abilityBind = KeyBindingHelper.registerKeyBinding(new KeyMapping("key." + Noellesroles.MOD_ID + ".ability",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.trainmurdermystery.keybinds"));

        ClientPlayNetworking.registerGlobalReceiver(BroadcastMessageS2CPacket.ID, (payload, context) -> {
            final var client = context.client();
            client.execute(() -> {
                if (client.player != null) {
                    // if (!isPlayerInAdventureMode(client.player))
                    // return;
                    currentBroadcastMessage = payload.content();
                    broadcastMessageTicks = GameConstants.getInTicks(0,
                            NoellesRolesConfig.HANDLER.instance().broadcasterMessageDuration);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(OpenIntroPayload.ID, (payload, context) -> {
            final var client = context.client();
            client.execute(() -> {
                if (client.player != null) {
                    client.setScreen(new RoleIntroduceScreen(client.player));
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(PlayerResetS2CPacket.ID, (payload, context) -> {
            final var client = context.client();
            client.execute(() -> {
                if (client.player != null) {
                    // client.player.sendSystemMessage(Component.translatable("screen.noellesroles.guess_role.reset")
                    // .withColor(Color.ORANGE.getRGB()));
                    GuessRoleScreen.clearData();
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(BloodConfigS2CPacket.ID, (payload, context) -> {
            bloodMain.enabled = payload.enabled();
            LoggerFactory.getLogger(this.getClass())
                    .info("Blood Particle status: " + (bloodMain.enabled ? "Enabled" : "Disabled"));
        });
        ClientPlayNetworking.registerGlobalReceiver(OpenLockGuiS2CPacket.ID, (payload, context) -> {
            final var client = context.client();
            client.execute(() -> {
                if (client.player != null) {
                    if (!isPlayerInAdventureMode(client.player))
                        return;
                    final var pos = payload.pos();
                    Minecraft.getInstance()
                            .setScreen(new LockGameScreen(pos, LockEntityManager.getInstance().getLockEntity(pos)));
                }
            });
        });
        Listen.registerEvents();
        InvisbleHandItem.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (roleGuessNoteClientBind.consumeClick()) {
                client.execute(() -> {
                    client.setScreen(new GuessRoleScreen());
                });
            }
            if (roleIntroClientBind.consumeClick()) {
                client.execute(() -> {
                    client.setScreen(new RoleIntroduceScreen(client.player));
                });
            }

            if (broadcastMessageTicks > 0) {
                broadcastMessageTicks--;
                if (broadcastMessageTicks <= 0) {
                    currentBroadcastMessage = null;
                }
            }

            if (!isPlayerInAdventureMode(client.player))
                return;
            insanityTime++;
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

                // FriendlyByteBuf data = PacketByteBufs.create();
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
                        client.setScreen(new BroadcasterScreen());
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

                    @SuppressWarnings("unused")
                    net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(
                            net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                            net.minecraft.world.item.component.CustomData.EMPTY);
                    // 非炸弹客始终不可见
                    return 0.0F;
                });

        OnMessageBelowMoneyRenderer.EVENT.register((minecraft, guiGraphics, deltaTracker) -> {
            if (TMMClient.gameComponent != null) {
                var role = TMMClient.gameComponent.getRole(minecraft.player);
                if (role != null) {
                    if (role.canUseKiller()) {
                        return new MutableComponentResult(
                                Component
                                        .translatable("message.tip.for_killer",
                                                Component.keybind("key." + TMM.MOD_ID + ".instinct"))
                                        .withStyle(ChatFormatting.WHITE));
                    }
                }
            }
            return null;
        });

        // 5. 注册实体渲染器
        registerEntityRenderers();

        // 6. 注册Screen
        registerScreens();

        // 7. 注册血粒子
        bloodMain.init();
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