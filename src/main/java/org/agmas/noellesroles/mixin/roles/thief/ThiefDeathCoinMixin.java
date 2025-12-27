package org.agmas.noellesroles.mixin.roles.thief;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class ThiefDeathCoinMixin {
    
    @Inject(method = "onDeath", at = @At("TAIL"))
    void thiefOnDeath(DamageSource damageSource, CallbackInfo ci) {
        PlayerEntity thief = (PlayerEntity) (Object) this;
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(thief.getWorld());
        
        if (gameWorldComponent.isRole(thief, ModRoles.THIEF)) {
            PlayerShopComponent thiefShop = PlayerShopComponent.KEY.get(thief);
            int coins = thiefShop.balance;
            
            if (coins <= 0) {
                return;
            }

            int killerTeamShare = coins / 2;

            List<UUID> killerUUIDs = gameWorldComponent.getAllKillerTeamPlayers();
            List<PlayerEntity> killers = new ArrayList<>();
                        for (UUID uuid : killerUUIDs) {
                PlayerEntity player = thief.getWorld().getPlayerByUuid(uuid);
                if (player != null && GameFunctions.isPlayerAliveAndSurvival(player)) {
                    killers.add(player);
                }
            }
            
            if (!killers.isEmpty()) {
                int coinsPerKiller = killerTeamShare / killers.size();
                int remainder = killerTeamShare % killers.size();
                
                for (PlayerEntity killer : killers) {
                    PlayerShopComponent killerShop = PlayerShopComponent.KEY.get(killer);
                    killerShop.balance += coinsPerKiller + (remainder-- > 0 ? 1 : 0);
                    killerShop.sync();
                }
            }
        }
    }
}