package org.agmas.noellesroles.screen;


import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import org.agmas.noellesroles.Noellesroles;

import java.util.UUID;

/**
 * 模组 ScreenHandler 注册
 */
public class ModScreenHandlers {
    
    /**
     * 邮差传递界面的 ScreenHandler 类型
     * 使用 ExtendedScreenHandlerType 来传递目标玩家的 UUID
     */
    public static final ExtendedScreenHandlerType<PostmanScreenHandler, UUID> POSTMAN_SCREEN_HANDLER =
        Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier.of(Noellesroles.MOD_ID, "postman"),
            new ExtendedScreenHandlerType<>(
                (syncId, playerInventory, data) -> new PostmanScreenHandler(syncId, playerInventory, data),
                Uuids.PACKET_CODEC.cast()
            )
        );
    
    /**
     * 私家侦探审查界面的 ScreenHandler 类型
     * 使用 ExtendedScreenHandlerType 来传递目标玩家的 UUID
     */
    public static final ExtendedScreenHandlerType<DetectiveInspectScreenHandler, UUID> DETECTIVE_INSPECT_SCREEN_HANDLER =
        Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier.of(Noellesroles.MOD_ID, "detective_inspect"),
            new ExtendedScreenHandlerType<>(
                (syncId, playerInventory, data) -> new DetectiveInspectScreenHandler(syncId, playerInventory, data),
                Uuids.PACKET_CODEC.cast()
            )
        );
    
    /**
     * 初始化并注册所有 ScreenHandler
     */
    public static void init() {
    }
}