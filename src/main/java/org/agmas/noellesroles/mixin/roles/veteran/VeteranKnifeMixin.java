package org.agmas.noellesroles.mixin.roles.veteran;


import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.VeteranPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入到 GameFunctions.killPlayer 方法
 * 用于处理退伍军人击杀后刀消失的逻辑
 */
@Mixin(GameFunctions.class)
public abstract class VeteranKnifeMixin {

    /**
     * 在玩家被杀死时触发
     * 检查是否是退伍军人用刀击杀，若是则移除刀
     */
    @Inject(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V", 
            at = @At("HEAD"))
    private static void onKillPlayerForVeteran(PlayerEntity victim, boolean spawnBody, PlayerEntity killer, Identifier deathReason, CallbackInfo ci) {
        if (killer == null) return;
        if (victim == null) return;
        if (killer.getWorld().isClient()) return;
        
        // 检查是否是刀击杀
        if (!deathReason.equals(GameConstants.DeathReasons.KNIFE)) return;
        
        // 检查击杀者是否是退伍军人
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(killer.getWorld());
        if (!gameWorld.isRole(killer, ModRoles.VETERAN)) return;
        
        // 获取退伍军人组件
        VeteranPlayerComponent veteranComp = ModComponents.VETERAN.get(killer);
        
        // 如果刀已经使用过，不处理（理论上不会到这里，因为刀已经被移除）
        if (veteranComp.knifeUsed) return;
        
        // 标记刀已使用
        veteranComp.markKnifeUsed();
        
        // 移除玩家手中的刀
        removeKnifeFromPlayer(killer);
    }
    
    /**
     * 从玩家身上移除刀
     */
    private static void removeKnifeFromPlayer(PlayerEntity player) {
        // 先检查主手
        ItemStack mainHand = player.getMainHandStack();
        if (mainHand.isOf(TMMItems.KNIFE)) {
            mainHand.setCount(0);
            return;
        }
        
        // 再检查副手
        ItemStack offHand = player.getOffHandStack();
        if (offHand.isOf(TMMItems.KNIFE)) {
            offHand.setCount(0);
            return;
        }
        
        // 最后遍历背包移除刀
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(TMMItems.KNIFE)) {
                stack.setCount(0);
                return;
            }
        }
    }
}