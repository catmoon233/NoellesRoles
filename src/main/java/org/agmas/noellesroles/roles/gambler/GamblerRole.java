package org.agmas.noellesroles.roles.gambler;

import static dev.doctor4t.trainmurdermystery.game.GameFunctions.getSpawnPos;
import static dev.doctor4t.trainmurdermystery.game.GameFunctions.roomToPlayer;
import net.minecraft.world.entity.Entity;

import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.events.ModdedRoleRemoved;
import org.agmas.noellesroles.NRSounds;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.screen.GamblerScreen;
import org.agmas.noellesroles.utils.RoleUtils;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.AreasWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.AnnounceWelcomePayload;
import dev.doctor4t.trainmurdermystery.util.GunShootPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class GamblerRole extends Role {

    public GamblerRole(ResourceLocation identifier, int color, boolean isInnocent, boolean canUseKiller,
            MoodType moodType, int maxSprintTime, boolean canSeeTime) {
        super(identifier, color, isInnocent, canUseKiller, moodType, maxSprintTime, canSeeTime);
    }

    @Override
    public void onPressAbilityKey(Object client) {
        if(client instanceof Minecraft mc)
        mc.setScreen(new GamblerScreen(mc.player));
    }

    @Override
    public boolean onUseGun(Player player) {
        if (player.level().isClientSide())
            return false;
        if (player.isShiftKeyDown()) {
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
            GamblerPlayerComponent gamblerPlayerComponent = GamblerPlayerComponent.KEY.get(player);
            gamblerPlayerComponent.usedAbility = true;

            if (gamblerPlayerComponent.selectedRole != null) {
                for (int i = 0; i < player.getInventory().items.size(); i++) {
                    if (player.getInventory().items.get(i).is(TMMItems.REVOLVER)) {
                        player.getInventory().items.set(i, ItemStack.EMPTY);
                    }
                }
                var role = RoleUtils.getRole(gamblerPlayerComponent.selectedRole);
                if (role == null) {
                    return false;
                }
                RoleUtils.changeRole(player, role);

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
                GameFunctions.killPlayer(player, true, null, Noellesroles.id("gamble_self_kill"));
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
