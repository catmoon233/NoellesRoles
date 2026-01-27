package org.agmas.noellesroles.component;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class RecorderPlayerComponent implements RoleComponent, ServerTickingComponent {

    public static final ComponentKey<RecorderPlayerComponent> KEY = ModComponents.RECORDER;

    private final Player player;

    private List<ResourceLocation> availableRoles = new ArrayList<>();

    private Map<UUID, ResourceLocation> guesses = new HashMap<>();

    private Map<UUID, String> startPlayers = new HashMap<>();

    private int wrongGuessCount = 0;
    private static final int MAX_WRONG_GUESSES = 10;

    private boolean rolesInitialized = false;
    private boolean wasRunning = false;

    public RecorderPlayerComponent(Player player) {
        this.player = player;
    }

    public void reset() {
        this.guesses.clear();
        this.availableRoles.clear();
        this.startPlayers.clear();
        this.wrongGuessCount = 0;
        this.rolesInitialized = false;
        ModComponents.RECORDER.sync(this.player);
    }

    public List<ResourceLocation> getAvailableRoles() {
        return availableRoles;
    }

    public Map<UUID, String> getStartPlayers() {
        return startPlayers;
    }

    public void setAvailableRoles(List<ResourceLocation> roles) {
        this.availableRoles = roles;
        this.rolesInitialized = true;
        ModComponents.RECORDER.sync(this.player);
    }

    public void addGuess(UUID targetUuid, ResourceLocation roleId) {
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        if (guesses.containsKey(targetUuid)) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.recorder.already_guessed")
                            .withStyle(ChatFormatting.RED),
                    true);
            return;
        }

        guesses.put(targetUuid, roleId);
        ModComponents.RECORDER.sync(this.player);

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        Player target = player.level().getPlayerByUUID(targetUuid);
        boolean isCorrect = false;

        int playerCount = player.level().players().size();
        int cooldownSeconds;
        if (playerCount < 10) {
            cooldownSeconds = 30;
        } else if (playerCount < 15) {
            cooldownSeconds = 25;
        } else if (playerCount < 20) {
            cooldownSeconds = 15;
        } else if (playerCount < 30) {
            cooldownSeconds = 10;
        } else {
            cooldownSeconds = 5;
        }
        serverPlayer.getCooldowns().addCooldown(ModItems.WRITTEN_NOTE, cooldownSeconds * 20);
        if (target != null) {
            Role actualRole = gameWorld.getRole(target);
            if (actualRole != null && actualRole.identifier().equals(roleId)) {
                isCorrect = true;
            }
        }

        if (isCorrect) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.recorder.correct_guess")
                            .withStyle(ChatFormatting.GREEN),
                    true);
            checkWinCondition();
        } else {
            wrongGuessCount++;
            serverPlayer.displayClientMessage(
                    Component
                            .translatable("message.noellesroles.recorder.wrong_guess", wrongGuessCount,
                                    MAX_WRONG_GUESSES)
                            .withStyle(ChatFormatting.RED),
                    true);

            if (wrongGuessCount >= MAX_WRONG_GUESSES) {
                // 猜错10次，立刻死亡
                GameFunctions.killPlayer(player, true, null, Noellesroles.id("recorder_mistake"));
                serverPlayer.displayClientMessage(
                        Component.translatable("message.noellesroles.recorder.died_from_mistakes")
                                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                        false);
            }
        }
    }

    public Map<UUID, ResourceLocation> getGuesses() {
        return guesses;
    }

    private void checkWinCondition() {
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        int correctGuesses = 0;

        for (Map.Entry<UUID, ResourceLocation> entry : guesses.entrySet()) {
            UUID targetUuid = entry.getKey();
            ResourceLocation guessedRoleId = entry.getValue();

            Player target = player.level().getPlayerByUUID(targetUuid);
            if (target != null) {
                Role actualRole = gameWorld.getRole(target);
                if (actualRole != null && actualRole.identifier().equals(guessedRoleId)) {
                    correctGuesses++;
                }
            }
        }

        int totalPlayers = player.level().players().size();
        int requiredCorrect = (int) Math.ceil(totalPlayers * 0.4);

        if (requiredCorrect < 2)
            requiredCorrect = 2;
        if (requiredCorrect > totalPlayers - 1)
            requiredCorrect = totalPlayers - 1;

        if (correctGuesses >= requiredCorrect) {
            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                GameRoundEndComponent.KEY.get(serverLevel).setRoundEndData(serverLevel.players(),
                        GameFunctions.WinStatus.RECORDER);
                GameFunctions.stopGame(serverLevel);
            }

            // 广播胜利消息
            for (Player p : player.level().players()) {
                p.displayClientMessage(
                        Component.translatable("message.noellesroles.recorder.win", player.getName())
                                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                        true);
            }
        }
    }

    @Override
    public void serverTick() {
        if (!rolesInitialized && player instanceof ServerPlayer) {
            initializeRoles();
        }
    }

    public void initializeRoles() {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel))
            return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (!gameWorld.isRole(player, ModRoles.RECORDER))
            return;

        // 初始化开局玩家列表（仅在为空时初始化）
        if (startPlayers.isEmpty()) {
            for (Player p : player.level().players()) {
                if (p.getUUID().equals(player.getUUID()))
                    continue;
                startPlayers.put(p.getUUID(), p.getName().getString());
            }
            ModComponents.RECORDER.sync(this.player);
        }

        updateAvailableRoles();
    }

    public void sync() {
        ModComponents.RECORDER.sync(this.player);
    }

    public void updateAvailableRoles() {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel))
            return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        List<ResourceLocation> roles = new ArrayList<>();
        for (Player p : player.level().players()) {
            if (p.getUUID().equals(player.getUUID()))
                continue;

            Role role = gameWorld.getRole(p);
            if (role != null) {
                if (!roles.contains(role.identifier())) {
                    roles.add(role.identifier());
                }
            }
        }

        setAvailableRoles(roles);
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        availableRoles.clear();
        if (tag.contains("availableRoles")) {
            ListTag list = tag.getList("availableRoles", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                availableRoles.add(ResourceLocation.tryParse(list.getString(i)));
            }
        }

        guesses.clear();
        if (tag.contains("guesses")) {
            CompoundTag guessesTag = tag.getCompound("guesses");
            for (String key : guessesTag.getAllKeys()) {
                try {
                    UUID targetUuid = UUID.fromString(key);
                    ResourceLocation roleId = ResourceLocation.tryParse(guessesTag.getString(key));
                    if (roleId != null) {
                        guesses.put(targetUuid, roleId);
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        startPlayers.clear();
        if (tag.contains("startPlayers")) {
            CompoundTag startPlayersTag = tag.getCompound("startPlayers");
            for (String key : startPlayersTag.getAllKeys()) {
                try {
                    UUID targetUuid = UUID.fromString(key);
                    String name = startPlayersTag.getString(key);
                    startPlayers.put(targetUuid, name);
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        wrongGuessCount = tag.getInt("wrongGuessCount");
        rolesInitialized = tag.getBoolean("rolesInitialized");
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        ListTag list = new ListTag();
        for (ResourceLocation id : availableRoles) {
            if (id != null) {
                list.add(StringTag.valueOf(id.toString()));
            }
        }
        tag.put("availableRoles", list);

        CompoundTag guessesTag = new CompoundTag();
        for (Map.Entry<UUID, ResourceLocation> entry : guesses.entrySet()) {
            if (entry.getValue() != null) {
                guessesTag.putString(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        tag.put("guesses", guessesTag);

        CompoundTag startPlayersTag = new CompoundTag();
        for (Map.Entry<UUID, String> entry : startPlayers.entrySet()) {
            startPlayersTag.putString(entry.getKey().toString(), entry.getValue());
        }
        tag.put("startPlayers", startPlayersTag);

        tag.putInt("wrongGuessCount", wrongGuessCount);
        tag.putBoolean("rolesInitialized", rolesInitialized);
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}