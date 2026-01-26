package org.agmas.noellesroles.utils;

import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.events.ModdedRoleRemoved;
import org.agmas.noellesroles.Noellesroles;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * 角色相关工具
 */
public class RoleUtils {
    /**
     * 获取角色的显示名称
     */
    public static void changeRole(Player player, Role role) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
        // 删除旧职业
        var oldRole = gameWorldComponent.getRole(player);
        if (oldRole != null) {
            ((ModdedRoleRemoved) ModdedRoleRemoved.EVENT.invoker()).removeModdedRole(player, oldRole);
        }
        // 给新职业
        gameWorldComponent.addRole(player, role);
        // 触发事件
        ((ModdedRoleAssigned) ModdedRoleAssigned.EVENT.invoker()).assignModdedRole(player, role);
    }

    public static Component getRoleName(ResourceLocation roleIdentifier) {
        String translationKey = "announcement.role." + roleIdentifier.getPath();
        return Component.translatable(translationKey);
    }

    public static Component getRoleName(Role role) {
        // 尝试获取翻译后的角色名称
        return getRoleName(role.identifier());
    }

    /**
     * 获取一个职业从他的路径
     * 
     * @return 返回Role
     */
    public static Role getRoleFromName(String roleName) {
        var roles = Noellesroles.id(roleName);
        return TMMRoles.ROLES.get(roles);
    }

    public static Role getRole(ResourceLocation role) {
        return TMMRoles.ROLES.get(role);
    }
}
