package org.agmas.noellesroles.mixin.roles.stalker;


import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.StalkerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入到 ServerPlayerEntity.attack 方法
 * 让跟踪者二阶段持刀左键攻击也能直接杀死玩家
 */
@Mixin(ServerPlayerEntity.class)
public abstract class StalkerLeftClickKillMixin {

    /**
     * 在玩家攻击实体时触发
     * 如果是跟踪者二阶段以上持刀攻击玩家，则直接击杀目标
     */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onStalkerKnifeAttack(Entity target, CallbackInfo ci) {
        ServerPlayerEntity attacker = (ServerPlayerEntity) (Object) this;
        
        // 检查目标是否是玩家
        if (!(target instanceof PlayerEntity targetPlayer)) return;
        
        // 检查目标是否存活
        if (!GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) return;
        
        // 获取跟踪者组件
        StalkerPlayerComponent stalkerComp = ModComponents.STALKER.get(attacker);
        
        // 检查是否是活跃的跟踪者且处于二阶段
        if (!stalkerComp.isActiveStalker()) return;
        if (stalkerComp.phase < 2) return;
        
        // 检查手持物品是否是刀
        ItemStack mainHand = attacker.getStackInHand(Hand.MAIN_HAND);
        if (!mainHand.isOf(TMMItems.KNIFE)) return;
        
        // 三阶段时不能用左键击杀，只能用突进
        if (stalkerComp.phase == 3 && stalkerComp.dashModeActive) {
            ci.cancel();
            return;
        }
        
        // 检查攻击是否在冷却中
        if (stalkerComp.isAttackOnCooldown()) {
            ci.cancel();
            return;
        }
        
        // 二阶段：左键直接击杀
        GameFunctions.killPlayer(targetPlayer, true, attacker, GameConstants.DeathReasons.KNIFE);
        
        // 触发攻击冷却
        stalkerComp.triggerAttackCooldown();
        
        // 击杀后定身0.5秒（10 tick）- 使用缓慢和失明来模拟
        attacker.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SLOWNESS, 10, 127, false, false, false
        ));
        attacker.addStatusEffect(new StatusEffectInstance(
            StatusEffects.MINING_FATIGUE, 10, 127, false, false, false
        ));
        
        // 取消原始攻击逻辑
        ci.cancel();
    }
}