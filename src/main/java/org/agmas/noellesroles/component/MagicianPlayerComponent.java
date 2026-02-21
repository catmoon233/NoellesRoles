package org.agmas.noellesroles.component;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerPsychoComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.network.RemoveStatusBarPayload;
import dev.doctor4t.trainmurdermystery.network.TriggerStatusBarPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.ModItems;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;

/**
 * 魔术师玩家组件
 * - 管理假疯狂模式状态(使用原版疯狂模式但给假球棒)
 * - 伪装身份：开局随机获得一个杀手身份（原版杀手和毒师除外）
 */
public class MagicianPlayerComponent implements RoleComponent, ServerTickingComponent {

    /** 组件键 */
    public static final ComponentKey<MagicianPlayerComponent> KEY = ModComponents.MAGICIAN;

    private final Player player;
    private ResourceLocation disguiseRoleId = null; // 伪装的角色ID

    @Override
    public Player getPlayer() {
        return player;
    }

    public MagicianPlayerComponent(Player player) {
        this.player = player;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer sp) {
        // 同步给魔术师自己和杀手阵营（让他们能看到伪装身份）
        if (this.player == sp) {
            return true; // 魔术师自己
        }
        
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(sp.level());
        if (gameWorld != null && gameWorld.canUseKillerFeatures(sp)) {
            return true; // 杀手阵营可以看到
        }
        
        return false; // 其他人不需要知道
    }

    /**
     * 启动假疯狂模式(使用原版疯狂模式但给假球棒)
     * 注意：商店会先给假球棒，这里只启动疯狂模式
     * @return 是否成功启动
     */
    public boolean startFakePsycho() {
        // 使用原版疯狂模式系统
        var psychoComponent = PlayerPsychoComponent.KEY.get(player);
        if (psychoComponent == null) {
            return false;
        }

        // 直接设置疯狂模式状态（不给球棒，因为商店已经给了假球棒）
        psychoComponent.setPsychoTicks(GameConstants.getPsychoTimer());
        psychoComponent.setArmour(GameConstants.getPsychoModeArmour());
        
        // 更新疯狂模式计数
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
        gameWorldComponent.setPsychosActive(gameWorldComponent.getPsychosActive() + 1);
        
        // 发送状态栏
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new TriggerStatusBarPayload("Psycho"));
        }
        
        // 同步魔术师组件状态到客户端
        MagicianPlayerComponent.KEY.sync(this.player);
        
        return true;
    }

    /**
     * 获取伪装的角色ID
     */
    public ResourceLocation getDisguiseRoleId() {
        return disguiseRoleId;
    }

    /**
     * 设置伪装的角色ID
     */
    public void setDisguiseRoleId(ResourceLocation roleId) {
        this.disguiseRoleId = roleId;
        MagicianPlayerComponent.KEY.sync(this.player);
    }

    @Override
    public void serverTick() {
        // 魔术师的疯狂模式由原版PlayerPsychoComponent处理
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        if (tag.contains("DisguiseRoleId")) {
            disguiseRoleId = ResourceLocation.tryParse(tag.getString("DisguiseRoleId"));
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        if (disguiseRoleId != null) {
            tag.putString("DisguiseRoleId", disguiseRoleId.toString());
        }
    }

    @Override
    public void reset() {
        disguiseRoleId = null;
        MagicianPlayerComponent.KEY.sync(this.player);
    }

    @Override
    public void clear() {
        this.reset();
    }
}


