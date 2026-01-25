package org.agmas.noellesroles.roles.gambler;

import static dev.doctor4t.trainmurdermystery.game.GameFunctions.getSpawnPos;
import static dev.doctor4t.trainmurdermystery.game.GameFunctions.roomToPlayer;

import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.noellesroles.NRSounds;
import org.agmas.noellesroles.client.utils.RoleUtils;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.AreasWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.tag.TMMItemTags;
import dev.doctor4t.trainmurdermystery.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class GamblerRole extends Role {

    public GamblerRole(ResourceLocation identifier, int color, boolean isInnocent, boolean canUseKiller,
            MoodType moodType, int maxSprintTime, boolean canSeeTime) {
        super(identifier, color, isInnocent, canUseKiller, moodType, maxSprintTime, canSeeTime);
    }

    @Override
    public boolean onUseGun(Player player) {
        if (player.level().isClientSide())
            return false;
        if (player.isShiftKeyDown()) {
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
            GamblerPlayerComponent gamblerPlayerComponent = GamblerPlayerComponent.KEY.get(player);
            gamblerPlayerComponent.usedAbility = true;
            player.getAllSlots().forEach(slot -> {
                if (slot.is(TMMItems.REVOLVER)) {
                    slot.setCount(0);
                }
            });

            if (gamblerPlayerComponent.selectedRole != null) {
                var role = RoleUtils.getRole(gamblerPlayerComponent.selectedRole);
                if (role == null) {
                    return false;
                }
                gameWorldComponent.addRole(player, role);

                ((ModdedRoleAssigned) ModdedRoleAssigned.EVENT.invoker()).assignModdedRole(player, role);

                PlayerShopComponent playerShopComponent = (PlayerShopComponent) PlayerShopComponent.KEY.get(player);
                playerShopComponent.setBalance(150);

                if (player instanceof ServerPlayer serverPlayer) {

                    final var size = gameWorldComponent.getAllKillerTeamPlayers().size();
                    ServerPlayNetworking.send(serverPlayer, new AnnounceWelcomePayload(
                            gameWorldComponent.getRole(player).getIdentifier().toString(), size, 0));
                    teleport(player);
                }
                player.level().players().forEach(
                        p -> {
                            p.playNotifySound(NRSounds.GAMBER_DEATH, SoundSource.PLAYERS, 0.5F, 1.3F);
                            p.playNotifySound(SoundEvents.BAT_HURT, SoundSource.PLAYERS, 0.5F, 1.3F);
                        });
            } else {
                player.kill();
            }
            return false;
        }
        return false;
    }

    private static void teleport(Player player) {

        Vec3 pos = getSpawnPos(AreasWorldComponent.KEY.get(player.level()), roomToPlayer.get(player.getUUID()));
        if (pos != null) {
            player.teleportTo(pos.x(), pos.y() + 1, pos.z());
        }

    }
}
