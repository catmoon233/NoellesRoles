package org.agmas.noellesroles.mixin.executioner;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.executioner.ExecutionerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * Executioner目标死亡时转变为杀手的Mixin
 * 当选定的目标死亡时，Executioner获胜并转变为随机杀手角色
 */
@Mixin(GameFunctions.class)
public class ExecutionerConfirmMixin {
    @Inject(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V", at = @At("HEAD"))
    private static void onPlayerKilled(PlayerEntity victim, boolean spawnBody, PlayerEntity killer, Identifier identifier, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(victim.getWorld());
        
        // 检查所有Executioner角色
        for (UUID uuid : gameWorldComponent.getAllWithRole(Noellesroles.EXECUTIONER)) {
            PlayerEntity executioner = victim.getWorld().getPlayerByUuid(uuid);
            if (executioner == null) continue;
            
            ExecutionerPlayerComponent executionerPlayerComponent = ExecutionerPlayerComponent.KEY.get(executioner);
            
            // 检查死亡的玩家是否是executioner的目标
            if (executionerPlayerComponent.target != null &&
                executionerPlayerComponent.target.equals(victim.getUuid()) &&
                !executionerPlayerComponent.won) {
                
                // 标记为获胜
                executionerPlayerComponent.won = true;
                // 解锁商店
                executionerPlayerComponent.unlockShop();
                
                // 给予商店余额（可选）
                PlayerShopComponent playerShopComponent = (PlayerShopComponent) PlayerShopComponent.KEY.get(executioner);
                playerShopComponent.setBalance(200);
                
                // 发送消息通知玩家商店已解锁
                executioner.sendMessage(net.minecraft.text.Text.translatable("message.executioner.shop_unlocked"));
            }
        }
    }
}
