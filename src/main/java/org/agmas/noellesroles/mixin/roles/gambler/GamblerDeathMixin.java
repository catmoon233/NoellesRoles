package org.agmas.noellesroles.mixin.roles.gambler;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.AreasWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.noellesroles.NRSounds;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.gambler.GamblerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;

import static dev.doctor4t.trainmurdermystery.game.GameFunctions.getSpawnPos;
import static dev.doctor4t.trainmurdermystery.game.GameFunctions.roomToPlayer;
import static org.agmas.noellesroles.role.ModRoles.EXECUTIONER_ID;

@Mixin(GameFunctions.class)
public class GamblerDeathMixin {
	@Inject(method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V", at = @At("HEAD"), cancellable = true)
	private static void onGamblerDeath(Player victim, boolean spawnBody, Player killer, ResourceLocation identifier, CallbackInfo ci) {
		if (identifier.getPath().equals("fell_out_of_train"))return;
		if (identifier.getPath().equals("disconnected"))return;
		final var world = victim.level();
		if (world.isClientSide)return;
		GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(world);
		if (gameWorldComponent.isRole(victim, ModRoles.GAMBLER)) {
			GamblerPlayerComponent gamblerPlayerComponent = GamblerPlayerComponent.KEY.get(victim);

			// 如果已经使用过能力，则正常死亡
			if (gamblerPlayerComponent.usedAbility) {
				return;
			}
			
			// 获取随机数决定结果 (0-99)
			int chance = victim.getRandom().nextInt(100);
			
			// 33%概率直接死亡 (0-32)
			if (chance < 33) {
				// 直接死亡，不取消事件
				return;
			}
			// 33%概率变为警长 (33-65)
			else if (chance < 66) {
				// 标记已使用能力
				gamblerPlayerComponent.usedAbility = true;
				gamblerPlayerComponent.sync();
				
				// 变成正义阵营（vigilante）
				victim.addItem(TMMItems.REVOLVER.getDefaultInstance());
				gameWorldComponent.addRole(victim, TMMRoles.VIGILANTE);
				ModdedRoleAssigned.EVENT.invoker().assignModdedRole(victim,TMMRoles.VIGILANTE);
				ServerPlayNetworking.send((ServerPlayer) victim, new AnnounceWelcomePayload(gameWorldComponent.getRole(victim).getIdentifier().toString(), gameWorldComponent.getAllKillerTeamPlayers().size(), 0));

				teleport(victim);
				// 取消死亡，玩家会在自己的房间复活
				ci.cancel();
			}
			// 33%概率变成杀手 (66-98)
			else if (chance < 99) {
				// 标记已使用能力
				gamblerPlayerComponent.usedAbility = true;
				gamblerPlayerComponent.sync();
				
				// 变成杀手阵营
				ArrayList<Role> shuffledKillerRoles = new ArrayList<>(Noellesroles.getEnableKillerRoles());
				shuffledKillerRoles.removeIf(role ->role.identifier().equals(EXECUTIONER_ID) || Harpymodloader.VANNILA_ROLES.contains(role) || !role.canUseKiller() || HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().getPath()));
				if (shuffledKillerRoles.isEmpty()) shuffledKillerRoles.add(TMMRoles.KILLER);
				Collections.shuffle(shuffledKillerRoles);

				final var first = shuffledKillerRoles.getFirst();
				gameWorldComponent.addRole(victim, first);
				ModdedRoleAssigned.EVENT.invoker().assignModdedRole(victim,TMMRoles.VIGILANTE);
				if (victim instanceof ServerPlayer serverPlayer) {
				//	final var size = serverPlayer.serverLevel().players().size();
					ServerPlayNetworking.send(serverPlayer, new AnnounceWelcomePayload(first.getIdentifier().toString(), gameWorldComponent.getAllKillerTeamPlayers().size(), 0));

				}
				PlayerShopComponent playerShopComponent = (PlayerShopComponent) PlayerShopComponent.KEY.get(victim);
				playerShopComponent.setBalance(150);
				// 取消死亡，玩家会在自己的房间复活
				teleport( victim);
				ci.cancel();
			}
			// 1%保留给用户自定义 (99)
			else {

				if (world instanceof ServerLevel serverWorld) {
					final var players = serverWorld.players();
					players.forEach(
							player -> {
								player.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.2F, 1.4F);
							}
					);
					GameRoundEndComponent.KEY.get(serverWorld).setRoundEndData(players, GameFunctions.WinStatus.GAMBLER);

					GameFunctions.stopGame(serverWorld);
				}
				return;
			}
			world.players().forEach(
					player -> {
						player.playNotifySound(NRSounds.GAMBER_DEATH, SoundSource.PLAYERS, 0.5F, 1.3F);
						player.playNotifySound(SoundEvents.BAT_HURT, SoundSource.PLAYERS, 0.5F, 1.3F);
					}
			);
		}
	}
	private static void teleport(Player player){

		Vec3 pos = getSpawnPos(AreasWorldComponent.KEY.get(player.level()), roomToPlayer.get(player.getUUID()));
		if (pos != null) {
			player.teleportTo(pos.x(), pos.y() + 1, pos.z());
		}

	}
}