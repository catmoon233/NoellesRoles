package org.agmas.noellesroles.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;

import org.agmas.noellesroles.role.ModRoles;

import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;

public class DeathPenaltyComponent implements RoleComponent {
    private final Player player;
    public long penaltyExpiry = 0;

    public void clearAll() {
        this.reset();
    }

    public void check() {
        if (!this.hasPenalty()) {
            return;
        } else {
            if (this.penaltyExpiry < 0) {
                GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
                boolean INSANE_alive = false;
                boolean CONSPIRATOR_alive = false;
                for (Player p : player.level().players()) {
                    if (gameWorldComponent.isRole(p, ModRoles.CONSPIRATOR)
                            && GameFunctions.isPlayerAliveAndSurvival(p)) {
                        CONSPIRATOR_alive = true;
                    } else if (gameWorldComponent.isRole(p,
                            ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)
                            && GameFunctions.isPlayerAliveAndSurvival(p)) {
                        INSANE_alive = true;
                    }
                    if (INSANE_alive && CONSPIRATOR_alive) {
                        if (this.penaltyExpiry == -2) {
                            this.penaltyExpiry = -1;
                            player.sendSystemMessage(
                                    Component.translatable("message.noellesroles.penalty.limit.god_job_couple")
                                            .withStyle(ChatFormatting.RED));
                            player.displayClientMessage(
                                    Component.translatable("message.noellesroles.penalty.limit.god_job_couple")
                                            .withStyle(ChatFormatting.RED),
                                    true);
                            if (player.hasPermissions(2)) {
                                player.sendSystemMessage(
                                        Component.translatable("message.noellesroles.admin.free_cam_hint")
                                                .withStyle(ChatFormatting.YELLOW));
                            }
                        }
                        return;
                    }
                }
                player.displayClientMessage(
                        Component.translatable("message.noellesroles.penalty.unlimit").withStyle(ChatFormatting.GREEN),
                        true);
                player.sendSystemMessage(
                        Component.translatable("message.noellesroles.penalty.unlimit").withStyle(ChatFormatting.GREEN));
                this.reset();
                return;
                // 亡语杀手限制
            } else {
                if (player.level().getGameTime() >= this.penaltyExpiry) {
                    player.displayClientMessage(Component.translatable("message.noellesroles.penalty.unlimit")
                            .withStyle(ChatFormatting.GREEN), true);
                    player.sendSystemMessage(Component.translatable("message.noellesroles.penalty.unlimit")
                            .withStyle(ChatFormatting.GREEN));
                    this.reset();
                    return;
                }
            }
        }
    }

    public DeathPenaltyComponent(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public void setPenalty(long durationTicks) {
        if (durationTicks < 0) {
            this.penaltyExpiry = -1;
            ModComponents.DEATH_PENALTY.sync(player);
            return;
        }
        this.penaltyExpiry = player.level().getGameTime() + durationTicks;
        ModComponents.DEATH_PENALTY.sync(player);
    }

    public boolean hasPenalty() {
        if (this.penaltyExpiry == 0)
            return false;
        if (this.penaltyExpiry < 0) {
            return true;
        }
        if (player.level().getGameTime() >= this.penaltyExpiry) {
            this.penaltyExpiry = -2;
        }
        return true;
    }

    @Override
    public void reset() {
        this.penaltyExpiry = 0;
        ModComponents.DEATH_PENALTY.sync(player);
    }

    @Override
    public void clear() {
        this.reset();
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.penaltyExpiry = tag.getLong("penaltyExpiry");
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putLong("penaltyExpiry", this.penaltyExpiry);
    }
}