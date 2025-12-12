package org.agmas.noellesroles.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.api.Role;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class SetRoleMaxCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var setmaxCommand = CommandManager.literal("noellesroles")
                    .then(CommandManager.literal("setmax")
                            .then(CommandManager.argument("role", IdentifierArgumentType.identifier())
                                    .suggests((context, builder) -> {
                                        for (Role role : TMMRoles.ROLES) {
                                            Identifier id = role.identifier();
                                            builder.suggest(id.toString());
                                        }
                                        return builder.buildFuture();
                                    })
                                    .then(CommandManager.argument("value", IntegerArgumentType.integer(0, 10))
                                            .executes(context -> {
                                                Identifier roleId = IdentifierArgumentType.getIdentifier(context, "role");
                                                int value = IntegerArgumentType.getInteger(context, "value");

                                                Role roleObj = null;
                                                for (Role role : TMMRoles.ROLES) {
                                                    if (role.identifier().equals(roleId)) {
                                                        roleObj = role;
                                                        break;
                                                    }
                                                }
                                                if (roleObj != null) {
                                                    Harpymodloader.setRoleMaximum(roleObj, value);
                                                } else {

                                                    Harpymodloader.setRoleMaximum(roleId, value);
                                                }

                                                NoellesRolesConfig config = NoellesRolesConfig.HANDLER.instance();
                                                boolean configUpdated = false;
                                                String rolePath = roleId.getPath();
                                                try {

                                                    Field field = getField(rolePath);
                                                    field.set(config, value);
                                                    configUpdated = true;
                                                } catch (Exception e) {
                                                }
                                                if (configUpdated) {
                                                    NoellesRolesConfig.HANDLER.save();
                                                }

                                                context.getSource().sendMessage(Text.literal("Set max " + roleId + " to " + value));
                                                return 1;
                                            }))));
            dispatcher.register(setmaxCommand);
        });
    }

    private static @NotNull Field getField(String rolePath) throws NoSuchFieldException {
        String[] parts = rolePath.split("_");
        StringBuilder fieldNameBuilder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i > 0) {
                // Capitalize first letter of subsequent parts
                part = part.substring(0, 1).toUpperCase() + part.substring(1);
            }
            fieldNameBuilder.append(part);
        }
        fieldNameBuilder.append("Max");
        String fieldName = fieldNameBuilder.toString();

        // Use reflection to set the field
        return NoellesRolesConfig.class.getField(fieldName);
    }
}