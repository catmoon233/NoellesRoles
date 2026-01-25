package org.agmas.noellesroles.client.utils;

import org.agmas.noellesroles.Noellesroles;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 角色相关工具 客户端
 */
public class RoleUtils {
    /**
     * 获取角色的显示名称
     */
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
