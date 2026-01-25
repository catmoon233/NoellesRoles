package org.agmas.noellesroles.client.utils;

import dev.doctor4t.trainmurdermystery.api.Role;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 角色相关工具 客户端
 */
public class RoleUtils {
    /**
     * 获取角色的显示名称
     */
    public static Component getRoleNameFromIdentifier(ResourceLocation roleIdentifier) {
        String translationKey = "announcement.role." + roleIdentifier.getPath();
        return Component.translatable(translationKey);
    }

    public static Component getRoleName(Role role) {
        // 尝试获取翻译后的角色名称
        return getRoleNameFromIdentifier(role.identifier());
    }
}
