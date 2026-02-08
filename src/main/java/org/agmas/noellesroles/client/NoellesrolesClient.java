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
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.network.BreakArmorPayload;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;

import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.blood.BloodMain;
import org.agmas.noellesroles.client.screen.*;
import org.agmas.noellesroles.client.event.MutableComponentResult;
import org.agmas.noellesroles.client.event.OnMessageBelowMoneyRenderer;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.entity.LockEntity;
import org.agmas.noellesroles.packet.*;
import org.agmas.noellesroles.role.ModRoles;
import org.lwjgl.glfw.GLFW;
import org.slf4j.LoggerFactory;
import walksy.crosshairaddons.CrosshairAddons;

import java.util.*;

import static org.agmas.noellesroles.client.RicesRoleRhapsodyClient.*;
import static org.agmas.noellesroles.component.InsaneKillerPlayerComponent.playerBodyEntities;

public class NoellesrolesClient implements ClientModInitializer {

    public static int insanityTime = 0;
    public static KeyMapping roleGuessNoteClientBind;
    public static KeyMapping abilityBind;
    public static KeyMapping roleIntroClientBind;
    public static Player target;
    public static PlayerBodyEntity targetBody;
    public static Player targetFakeBody;
    public static Player hudTarget;

    public static Map<UUID, UUID> SHUFFLED_PLAYER_ENTRIES_CACHE = Maps.newHashMap();
    public static Component currentBroadcastMessage = null;
    public static int broadcastMessageTicks = 0;
    public static BloodMain bloodMain = new BloodMain();


    private static long lastClientTickTime = 0;
    private static final long CLIENT_TICK_INTERVAL_MS = 50; // 1000ms / 20 ticks per second = 50ms per tick

    @Override
    public void onInitializeClient() {
        InstinctRenderer.registerInstinctEvents();
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
        ClientPlayNetworking.registerGlobalReceiver(BreakArmorPayload.ID, (payload, context) -> {
            final var client = context.client();
            client.execute(() -> {
                if (client.player != null) {
                    CrosshairAddons.getStateManager().handleBreakPacket(payload.x(), payload.y(), payload.z());
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
                    BlockPos pos = payload.pos();
                    int x = pos.getX();
                    int lockLength = payload.lockLength();
                    int y = pos.getY();
                    int z = pos.getZ();
                    UUID entityId = payload.lockId();
                    AABB areas = new AABB(
                            x - 5, y - 5, z - 5,
                            x + 5, y + 5, z + 5);
                    var entities = Minecraft.getInstance().level.getEntities(client.player, areas, (entity) -> {
                        if (entity instanceof LockEntity) {
                            return true;
                        }
                        return false;
                    });
                    Entity lockEntity = null;
                    for (var entity : entities) {
                        if (entity.getUUID().equals(entityId)) {
                            lockEntity = entity;
                        }
                    }
                    if (lockEntity != null && lockEntity instanceof LockEntity lock) {
                        lock.setLength(lockLength);
                        Minecraft.getInstance()
                                .setScreen(new LockGameScreen(pos, lock));
                    }

                }
            });
        });
        // 注册抽奖网络包处理：接收服务器抽奖结果后播放抽奖动画
        ClientPlayNetworking.registerGlobalReceiver(LootResultS2CPacket.ID, (payload, context)->{
            final var client = context.client();
            client.execute(() -> {
                if (client.player != null) {
                    client.setScreen(new LootScreen(payload.ansID()));
                }
            });
        });
        // 注册抽奖界面网络包处理：接收服务器奖池信息并显示界面
        ClientPlayNetworking.registerGlobalReceiver(LootInfoScreenS2CPacket.ID, (payload, context)->{
            final var client = context.client();
            client.execute(() -> {
                if (client.player != null) {
                    client.setScreen(new LootInfoScreen());
                }
            });
        });

        Listen.registerEvents();
        InvisbleHandItem.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClientTickTime >= CLIENT_TICK_INTERVAL_MS) {
                lastClientTickTime = currentTime;
                playerBodyEntities.forEach(
                        (uuid, playerBodyEntity) -> {
                            if (playerBodyEntity.getPlayerUuid().equals(uuid))
                                ++playerBodyEntity.tickCount;
                        });

            }
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
            tooltipHelper(TMMItems.DEFENSE_VIAL, itemStack, list);
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