package org.agmas.noellesroles.mixin.roles.veteran;


import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    @Inject(method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V", 
            at = @At("HEAD"))
    private static void onKillPlayerForVeteran(Player victim, boolean spawnBody, Player killer, ResourceLocation deathReason, CallbackInfo ci) {
        if (killer == null) return;
        if (victim == null) return;
        if (killer.level().isClientSide()) return;
        
        // 检查是否是刀击杀
        if (!deathReason.equals(GameConstants.DeathReasons.KNIFE)) return;
        
        // 检查击杀者是否是退伍军人
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(killer.level());
        if (!gameWorld.isRole(killer, ModRoles.VETERAN)) return;

        // 获取退伍军人组件
        VeteranPlayerComponent veteranComp = ModComponents.VETERAN.get(killer);
        
        // 如果刀已经使用过，不处理（理论上不会到这里，因为刀已经被移除）
        if (veteranComp.knifeUsed) return;
        
        // 标记刀已使用
        veteranComp.markKnifeUsed();
        
        // 移除玩家手中的刀
        removeKnifeFromPlayer(killer);
        if (gameWorld.isInnocent( victim)){
            GameFunctions.killPlayer(killer, true, killer, TMM.id("shot_innocent"));

        }
    }
    
    /**
     * 从玩家身上移除刀
     */
    private static void removeKnifeFromPlayer(Player player) {
        // 先检查主手
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.is(TMMItems.KNIFE)) {
            mainHand.setCount(0);
            return;
        }
        
        // 再检查副手
        ItemStack offHand = player.getOffhandItem();
        if (offHand.is(TMMItems.KNIFE)) {
            offHand.setCount(0);
            return;
        }
        
        // 最后遍历背包移除刀
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(TMMItems.KNIFE)) {
                stack.setCount(0);
                return;
            }
        }
    }
}