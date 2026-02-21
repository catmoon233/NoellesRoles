package org.agmas.noellesroles.mixin.client.magician;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.client.gui.RoleNameRenderer;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.component.ModComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 魔术师角色名称渲染Mixin
 * - 杀手查看魔术师时，会看到魔术师伪装的杀手身份
 */
@Mixin(RoleNameRenderer.class)
public abstract class MagicianRoleNameRendererMixin {

    /**
     * 当杀手查看魔术师时，显示为伪装的杀手角色
     */
    @WrapOperation(method = "renderHud", at = @At(value = "INVOKE", target = "Ldev/doctor4t/trainmurdermystery/cca/GameWorldComponent;getRole(Lnet/minecraft/world/entity/player/Player;)Ldev/doctor4t/trainmurdermystery/api/Role;"))
    private static Role getMagicianDisguiseRole(GameWorldComponent instance, Player target, Operation<Role> original) {
        Role originalRole = original.call(instance, target);
        
        // 如果当前玩家是杀手（或能使用杀手功能的角色）
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return originalRole;
        }
        
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.level);
        if (!gameWorld.canUseKillerFeatures(client.player)) {
            // 只有杀手能看到魔术师的伪装身份
            return originalRole;
        }
        
        // 如果目标是魔术师，返回伪装的角色
        Role magicianRole = TMMRoles.ROLES.get(ModRoles.MAGICIAN_ID);
        if (magicianRole != null && gameWorld.isRole(target, magicianRole)) {
            var magicianComponent = ModComponents.MAGICIAN.get(target);
            if (magicianComponent != null) {
                ResourceLocation disguiseId = magicianComponent.getDisguiseRoleId();
                if (disguiseId != null) {
                    Role disguiseRole = TMMRoles.ROLES.get(disguiseId);
                    if (disguiseRole != null) {
                        return disguiseRole;
                    }
                }
            }
        }
        
        return originalRole;
    }
}