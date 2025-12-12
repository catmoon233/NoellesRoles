package org.agmas.noellesroles.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.config.NoellesRolesConfig;

public class ConfigCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var configCommand = CommandManager.literal("noellesroles")
                    .then(CommandManager.literal("config")
                            .requires(source -> source.hasPermissionLevel(2)) // 需要OP权限
                            .then(CommandManager.literal("reload")
                                    .executes(context -> {
                                        NoellesRolesConfig.HANDLER.load();
                                        context.getSource().sendMessage(Text.literal("NoellesRoles configuration reloaded successfully"));
                                        return 1;
                                    }))
                            .then(CommandManager.literal("reset")
                                    .executes(context -> {
                                        // 创建默认配置实例
                                        NoellesRolesConfig defaultConfig = new NoellesRolesConfig();
                                        // 将当前配置重置为默认值
                                        NoellesRolesConfig config = NoellesRolesConfig.HANDLER.instance();
                                        
                                        try {
                                            // 使用反射自动重置所有配置字段
                                            for (java.lang.reflect.Field field : NoellesRolesConfig.class.getDeclaredFields()) {
                                                // 跳过静态字段
                                                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                                                    continue;
                                                }
                                                // 设置可访问
                                                field.setAccessible(true);
                                                // 从默认配置中获取值并设置到当前配置
                                                Object defaultValue = field.get(defaultConfig);
                                                field.set(config, defaultValue);
                                            }
                                            
                                            // 保存到文件
                                            NoellesRolesConfig.HANDLER.save();
                                            
                                            context.getSource().sendMessage(Text.literal("NoellesRoles configuration reset to defaults successfully"));
                                        } catch (Exception e) {
                                            context.getSource().sendMessage(Text.literal("Failed to reset configuration: " + e.getMessage()));
                                            return 0;
                                        }
                                        
                                        return 1;
                                    })));
            dispatcher.register(configCommand);
        });
    }
}