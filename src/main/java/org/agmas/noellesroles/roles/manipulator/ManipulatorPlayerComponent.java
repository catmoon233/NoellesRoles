
package org.agmas.noellesroles.roles.manipulator;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.noellesroles.ModEntities;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.entity.ManipulatorBodyEntity;
import org.agmas.noellesroles.role.ModRoles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;

/**
 * 操纵师组件
 */
public class ManipulatorPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {

    public static final ComponentKey<ManipulatorPlayerComponent> KEY = ComponentRegistry.getOrCreate(
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "manipulator"),
            ManipulatorPlayerComponent.class);

    public static final int CONTROL_DURATION = 50 * 20;

    public static final int CONTROL_COOLDOWN = 60 * 20;

    // ==================== 状态变量 ====================

    private final Player player;

    public UUID target;

    public boolean isControlling;

    public int controlTimer;

    public int cooldown;

    public UUID bodyEntityUuid;

    public Vec3 originalPosition = Vec3.ZERO;

    public float originalYaw = 0;
    public float originalPitch = 0;

    public NonNullList<ItemStack> originalInventory = NonNullList.withSize(41, ItemStack.EMPTY);

    public int originalBalance = 0;

    public GameType victimOriginalGameMode = GameType.SURVIVAL;

    public Role victimOriginalRole = null;

    public NonNullList<ItemStack> victimOriginalInventory = NonNullList.withSize(41, ItemStack.EMPTY);

    public int victimOriginalBalance = 0;

    public boolean isManipulatorMarked = false;

    public UUID victimSkinUuid = null;

    public ManipulatorPlayerComponent(Player player) {
        this.player = player;
        this.target = null;
        this.isControlling = false;
        this.controlTimer = 0;
        this.cooldown = 0;
    }

    public void reset() {
        this.target = null;
        this.isControlling = false;
        this.controlTimer = 0;
        this.cooldown = 0;
        this.bodyEntityUuid = null;
        this.originalPosition = Vec3.ZERO;
        this.originalYaw = 0;
        this.originalPitch = 0;
        this.originalInventory = NonNullList.withSize(41, ItemStack.EMPTY);
        this.originalBalance = 0;
        this.victimOriginalGameMode = GameType.SURVIVAL;
        this.victimOriginalRole = null;
        this.victimOriginalInventory = NonNullList.withSize(41, ItemStack.EMPTY);
        this.victimOriginalBalance = 0;
        this.isManipulatorMarked = true;
        this.victimSkinUuid = null;
        this.sync();
    }

    public void clearAll() {
        this.target = null;
        this.isControlling = false;
        this.controlTimer = 0;
        this.cooldown = 0;
        this.bodyEntityUuid = null;
        this.originalPosition = Vec3.ZERO;
        this.originalYaw = 0;
        this.originalPitch = 0;
        this.originalInventory = NonNullList.withSize(41, ItemStack.EMPTY);
        this.originalBalance = 0;
        this.victimOriginalGameMode = GameType.SURVIVAL;
        this.victimOriginalRole = null;
        this.victimOriginalInventory = NonNullList.withSize(41, ItemStack.EMPTY);
        this.victimOriginalBalance = 0;
        this.isManipulatorMarked = false;
        this.victimSkinUuid = null;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public boolean canUseAbility() {
        return isManipulatorMarked && !isControlling && cooldown <= 0;
    }

    /**
     * 
     * @param targetUuid
     */
    public void setTarget(UUID targetUuid) {
        if (!canUseAbility())
            return;
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        Player targetPlayer = player.level().getPlayerByUUID(targetUuid);
        if (targetPlayer == null || !(targetPlayer instanceof ServerPlayer serverTarget))
            return;

        if (targetUuid.equals(player.getUUID()))
            return;

        if (!GameFunctions.isPlayerAliveAndSurvival(targetPlayer))
            return;

        this.target = targetUuid;

        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();

        saveInventory(originalInventory, player);

        PlayerShopComponent manipulatorShop = PlayerShopComponent.KEY.get(player);
        originalBalance = manipulatorShop.balance;

        createBodyMarker(serverPlayer);

        victimOriginalGameMode = serverTarget.gameMode.getGameModeForPlayer();
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        victimOriginalRole = gameWorld.getRole(targetPlayer);
        saveInventory(victimOriginalInventory, targetPlayer);
        PlayerShopComponent victimShop = PlayerShopComponent.KEY.get(targetPlayer);
        victimOriginalBalance = victimShop.balance;
        victimSkinUuid = targetUuid;

        serverTarget.setGameMode(GameType.SPECTATOR);

        serverTarget.setCamera(serverPlayer);

        loadInventory(victimOriginalInventory, player);
        manipulatorShop.setBalance(victimOriginalBalance);
        manipulatorShop.sync();

        isControlling = true;
        controlTimer = CONTROL_DURATION;

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 1.0F);

        this.sync();
    }

    private void createBodyMarker(ServerPlayer serverPlayer) {
        ServerLevel world = serverPlayer.serverLevel();

        ManipulatorBodyEntity bodyEntity = new ManipulatorBodyEntity(ModEntities.MANIPULATOR_BODY, world);
        bodyEntity.setPos(originalPosition);
        bodyEntity.setYRot(originalYaw);
        bodyEntity.setXRot(originalPitch);
        bodyEntity.setOwner(serverPlayer);

        world.addFreshEntity(bodyEntity);
        bodyEntityUuid = bodyEntity.getUUID();
    }

    private void removeBodyMarker() {
        if (bodyEntityUuid == null)
            return;
        if (!(player.level() instanceof ServerLevel world))
            return;

        Entity entity = world.getEntity(bodyEntityUuid);
        if (entity != null) {
            entity.discard();
        }
        bodyEntityUuid = null;
    }

    private void saveInventory(NonNullList<ItemStack> storage, Player targetPlayer) {
        for (int i = 0; i < Math.min(storage.size(), targetPlayer.getInventory().getContainerSize()); i++) {
            storage.set(i, targetPlayer.getInventory().getItem(i).copy());
        }
    }

    private void loadInventory(NonNullList<ItemStack> storage, Player targetPlayer) {
        targetPlayer.getInventory().clearContent();
        for (int i = 0; i < Math.min(storage.size(), targetPlayer.getInventory().getContainerSize()); i++) {
            targetPlayer.getInventory().setItem(i, storage.get(i).copy());
        }
    }

    /**
     * 停止操控
     * 
     * @param timeout
     */
    public void stopControl(boolean timeout) {
        if (!isControlling)
            return;
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        Player targetPlayer = player.level().getPlayerByUUID(target);

        removeBodyMarker();

        if (targetPlayer instanceof ServerPlayer serverTarget) {
            serverTarget.setGameMode(victimOriginalGameMode);

            serverTarget.setCamera(serverTarget);

            loadInventory(victimOriginalInventory, targetPlayer);
            PlayerShopComponent victimShop = PlayerShopComponent.KEY.get(targetPlayer);
            victimShop.setBalance(victimOriginalBalance);
            victimShop.sync();

            serverTarget.displayClientMessage(
                    Component.translatable("message.noellesroles.manipulator.control_ended")
                            .withStyle(ChatFormatting.GREEN),
                    true);
        }

        serverPlayer.teleportTo(
                serverPlayer.serverLevel(),
                originalPosition.x, originalPosition.y, originalPosition.z,
                originalYaw, originalPitch);

        loadInventory(originalInventory, player);
        PlayerShopComponent manipulatorShop = PlayerShopComponent.KEY.get(player);
        manipulatorShop.setBalance(originalBalance);
        manipulatorShop.sync();

        isControlling = false;
        controlTimer = 0;
        target = null;
        victimSkinUuid = null;

        // 设置冷却
        cooldown = CONTROL_COOLDOWN;

        // 发送消息
        if (timeout) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.manipulator.control_timeout")
                            .withStyle(ChatFormatting.YELLOW),
                    true);
        } else {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.manipulator.control_stopped")
                            .withStyle(ChatFormatting.GREEN),
                    true);
        }

        // 播放音效
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

        this.sync();
    }

    /**
     * 本体死亡处理
     * 当本体标记被破坏或本体实体死亡时调用
     */
    public void onBodyDeath() {
        if (!isControlling)
            return;
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        // 移除本体标记实体
        removeBodyMarker();

        // 恢复被操控玩家状态
        Player targetPlayer = player.level().getPlayerByUUID(target);
        if (targetPlayer instanceof ServerPlayer serverTarget) {
            // 恢复游戏模式
            serverTarget.setGameMode(victimOriginalGameMode);

            // 停止附身
            serverTarget.setCamera(serverTarget);

            // 恢复被操控玩家的物品栏和金币
            loadInventory(victimOriginalInventory, targetPlayer);
            PlayerShopComponent victimShop = PlayerShopComponent.KEY.get(targetPlayer);
            victimShop.setBalance(victimOriginalBalance);
            victimShop.sync();

            // 发送消息
            serverTarget.displayClientMessage(
                    Component.translatable("message.noellesroles.manipulator.control_ended")
                            .withStyle(ChatFormatting.GREEN),
                    true);
        }

        isControlling = false;
        controlTimer = 0;
        target = null;
        victimSkinUuid = null;

        GameFunctions.killPlayer(serverPlayer, true, null);

        serverPlayer.displayClientMessage(
                Component.translatable("message.noellesroles.manipulator.body_died")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                false);

        this.sync();
    }

    public boolean isBodyAlive() {
        if (!isControlling || bodyEntityUuid == null)
            return true;
        if (!(player.level() instanceof ServerLevel world))
            return true;

        Entity bodyMarker = world.getEntity(bodyEntityUuid);
        return bodyMarker != null && bodyMarker.isAlive();
    }

    public boolean isActiveManipulator() {
        return isManipulatorMarked;
    }

    public float getControlSeconds() {
        return controlTimer / 20.0f;
    }

    public float getCooldownSeconds() {
        return cooldown / 20.0f;
    }

    @Override
    public void serverTick() {
        if (!isActiveManipulator())
            return;

        if (!GameFunctions.isPlayerAliveAndSurvival(player))
            return;

        if (cooldown > 0) {
            cooldown--;
            if (cooldown % 20 == 0 || cooldown == 0) {
                sync();
            }
        }

        if (isControlling) {
            if (!isBodyAlive()) {
                onBodyDeath();
                return;
            }

            Player targetPlayer = player.level().getPlayerByUUID(target);
            if (targetPlayer == null) {
                stopControl(false);
                return;
            }

            if (targetPlayer instanceof ServerPlayer serverTarget) {
                if (serverTarget.getCamera() != player) {
                    serverTarget.setCamera((ServerPlayer) player);
                }
            }

            if (controlTimer > 0) {
                controlTimer--;

                if (controlTimer % 20 == 0) {
                    sync();
                }

                if (controlTimer <= 0) {
                    stopControl(true);
                }
            }
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        if (this.target != null) {
            tag.putUUID("target", this.target);
        }
        tag.putBoolean("isControlling", this.isControlling);
        tag.putInt("controlTimer", this.controlTimer);
        tag.putInt("cooldown", this.cooldown);

        if (this.bodyEntityUuid != null) {
            tag.putUUID("bodyEntityUuid", this.bodyEntityUuid);
        }

        tag.putDouble("originalPosX", this.originalPosition.x);
        tag.putDouble("originalPosY", this.originalPosition.y);
        tag.putDouble("originalPosZ", this.originalPosition.z);
        tag.putFloat("originalYaw", this.originalYaw);
        tag.putFloat("originalPitch", this.originalPitch);
        tag.putInt("originalBalance", this.originalBalance);

        tag.putInt("victimGameMode", this.victimOriginalGameMode.getId());
        if (this.victimOriginalRole != null) {
            tag.putString("victimRole", this.victimOriginalRole.identifier().toString());
        }
        tag.putInt("victimBalance", this.victimOriginalBalance);

        if (this.victimSkinUuid != null) {
            tag.putUUID("victimSkinUuid", this.victimSkinUuid);
        }

        tag.putBoolean("isManipulatorMarked", this.isManipulatorMarked);

        ListTag originalInvTag = new ListTag();
        for (int i = 0; i < originalInventory.size(); i++) {
            if (!originalInventory.get(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                originalInvTag.add(originalInventory.get(i).save(registryLookup, itemTag));
            }
        }
        tag.put("originalInventory", originalInvTag);

        ListTag victimInvTag = new ListTag();
        for (int i = 0; i < victimOriginalInventory.size(); i++) {
            if (!victimOriginalInventory.get(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                victimInvTag.add(victimOriginalInventory.get(i).save(registryLookup, itemTag));
            }
        }
        tag.put("victimInventory", victimInvTag);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.target = tag.contains("target") ? tag.getUUID("target") : null;
        this.isControlling = tag.contains("isControlling") && tag.getBoolean("isControlling");
        this.controlTimer = tag.contains("controlTimer") ? tag.getInt("controlTimer") : 0;
        this.cooldown = tag.contains("cooldown") ? tag.getInt("cooldown") : 0;

        this.bodyEntityUuid = tag.contains("bodyEntityUuid") ? tag.getUUID("bodyEntityUuid") : null;

        double posX = tag.contains("originalPosX") ? tag.getDouble("originalPosX") : 0;
        double posY = tag.contains("originalPosY") ? tag.getDouble("originalPosY") : 0;
        double posZ = tag.contains("originalPosZ") ? tag.getDouble("originalPosZ") : 0;
        this.originalPosition = new Vec3(posX, posY, posZ);
        this.originalYaw = tag.contains("originalYaw") ? tag.getFloat("originalYaw") : 0;
        this.originalPitch = tag.contains("originalPitch") ? tag.getFloat("originalPitch") : 0;
        this.originalBalance = tag.contains("originalBalance") ? tag.getInt("originalBalance") : 0;

        this.victimOriginalGameMode = GameType.byId(tag.contains("victimGameMode") ? tag.getInt("victimGameMode") : 0);
        if (tag.contains("victimRole")) {
            String roleId = tag.getString("victimRole");
            ResourceLocation roleLocation = ResourceLocation.tryParse(roleId);
            if (roleLocation != null) {
                for (var role : TMMRoles.ROLES) {
                    if (role.identifier().equals(roleLocation)) {
                        this.victimOriginalRole = role;
                        break;
                    }
                }
            }
        }
        this.victimOriginalBalance = tag.contains("victimBalance") ? tag.getInt("victimBalance") : 0;

        this.victimSkinUuid = tag.contains("victimSkinUuid") ? tag.getUUID("victimSkinUuid") : null;

        this.isManipulatorMarked = tag.contains("isManipulatorMarked") && tag.getBoolean("isManipulatorMarked");

        this.originalInventory = NonNullList.withSize(41, ItemStack.EMPTY);
        if (tag.contains("originalInventory")) {
            ListTag originalInvTag = tag.getList("originalInventory", 10);
            for (int i = 0; i < originalInvTag.size(); i++) {
                CompoundTag itemTag = originalInvTag.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < originalInventory.size()) {
                    originalInventory.set(slot, ItemStack.parseOptional(registryLookup, itemTag));
                }
            }
        }

        this.victimOriginalInventory = NonNullList.withSize(41, ItemStack.EMPTY);
        if (tag.contains("victimInventory")) {
            ListTag victimInvTag = tag.getList("victimInventory", 10);
            for (int i = 0; i < victimInvTag.size(); i++) {
                CompoundTag itemTag = victimInvTag.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < victimOriginalInventory.size()) {
                    victimOriginalInventory.set(slot, ItemStack.parseOptional(registryLookup, itemTag));
                }
            }
        }
    }
}