package org.agmas.noellesroles.modifier;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.agmas.harpymodloader.events.ModifierRemoved;
import org.agmas.harpymodloader.events.ResetPlayerEvent;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.modifier.expedition.ExpeditionComponent;
import org.agmas.noellesroles.role.ModRoles;

import java.awt.Color;

/**
 * NoellesRoles 修饰符注册类
 */
public class NRModifiers {

    /** 远征队修饰符 */
    public static Modifier EXPEDITION = HMLModifiers.registerModifier(new Modifier(
            Noellesroles.id("expedition"),
            new Color(210, 180, 140).getRGB(), // 棕色 - 代表远征
            null,
            null,
            false,
            false));

    /**
     * 初始化修饰符系统
     */
    public static void init() {
        assignModifierComponents();
    }

    /**
     * 分配修饰符组件
     */
    public static void assignModifierComponents() {
        // 远征队修饰符分配事件
        ModifierAssigned.EVENT.register((player, modifier) -> {
            if (!modifier.equals(EXPEDITION)) {
                return;
            }
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }

            var level = serverPlayer.serverLevel();
            var gameWorld = GameWorldComponent.KEY.get(level);

            // 检查玩家是否是好人阵营（包括平民和警长阵营）
            // 并且不能是杀手阵营或中立阵营
            Role role = gameWorld.getRole(player);
            if (role != null && role.isInnocent() && !role.canUseKiller() && !role.isNeutrals()) {
                // 只排除小透明
                if (!gameWorld.isRole(player, ModRoles.GHOST)) {
                    // 给玩家分配远征队组件
                    var expeditionComponent = ExpeditionComponent.KEY.get(player);
                    expeditionComponent.sync();

                    Noellesroles.LOGGER.info("Expedition modifier assigned to player: " + player.getName().getString());
                }
            } else {
                Noellesroles.LOGGER.info("Expedition modifier not assigned to killer/neutral: " + player.getName().getString());
            }
        });

        // 远征队修饰符移除事件
        ModifierRemoved.EVENT.register((player, modifier) -> {
            if (modifier.equals(EXPEDITION)) {
                var expeditionComponent = ExpeditionComponent.KEY.get(player);
                if (expeditionComponent != null) {
                    expeditionComponent.clear();
                    expeditionComponent.sync();
                }
            }
        });

        // 玩家重置事件 - 清理远征队修饰符组件
        ResetPlayerEvent.EVENT.register(player -> {
            try {
                var expeditionComponent = ExpeditionComponent.KEY.get(player);
                if (expeditionComponent != null) {
                    expeditionComponent.clear();
                    expeditionComponent.sync();
                }
            } catch (Exception e) {
                // 玩家可能没有 expedition 组件，忽略错误
            }
        });
    }
}
