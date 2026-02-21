package org.agmas.noellesroles.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;

/**
 * 魔术师玩家组件
 * - 管理假疯狂模式状态
 * - 假疯狂模式：获得假球棒，穿上疯狂模式外观，但不播放音乐
 * - 伪装身份：开局随机获得一个杀手身份（原版杀手和毒师除外）
 */
public class MagicianPlayerComponent implements RoleComponent, ServerTickingComponent {

    /** 组件键 */
    public static final ComponentKey<MagicianPlayerComponent> KEY = ModComponents.MAGICIAN;

    private final Player player;
    private int fakePsychoTicks = 0; // 假疯狂模式剩余tick
    private ResourceLocation disguiseRoleId = null; // 伪装的角色ID

    @Override
    public Player getPlayer() {
        return player;
    }

    public MagicianPlayerComponent(Player player) {
        this.player = player;
    }

    /**
     * 启动假疯狂模式
     * @return 是否成功启动
     */
    public boolean startFakePsycho() {
        this.fakePsychoTicks = 30 * 20; // 30秒 = 600 tick
        MagicianPlayerComponent.KEY.sync(this.player);
        return true;
    }

    /**
     * 停止假疯狂模式
     */
    public void stopFakePsycho() {
        this.fakePsychoTicks = 0;
        MagicianPlayerComponent.KEY.sync(this.player);
    }

    /**
     * 获取假疯狂模式剩余tick
     */
    public int getFakePsychoTicks() {
        return fakePsychoTicks;
    }

    /**
     * 是否处于假疯狂模式
     */
    public boolean isFakePsychoActive() {
        return fakePsychoTicks > 0;
    }

    /**
     * 设置伪装的角色ID
     */
    public void setDisguiseRoleId(ResourceLocation roleId) {
        this.disguiseRoleId = roleId;
        MagicianPlayerComponent.KEY.sync(this.player);
    }

    /**
     * 获取伪装的角色ID
     */
    public ResourceLocation getDisguiseRoleId() {
        return disguiseRoleId;
    }

    @Override
    public void serverTick() {
        if (fakePsychoTicks > 0) {
            fakePsychoTicks--;
            if (fakePsychoTicks == 0) {
                // 倒计时结束，同步到客户端
                MagicianPlayerComponent.KEY.sync(this.player);
            }
        }
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        fakePsychoTicks = tag.getInt("FakePsychoTicks");
        if (tag.contains("DisguiseRoleId")) {
            disguiseRoleId = ResourceLocation.tryParse(tag.getString("DisguiseRoleId"));
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("FakePsychoTicks", fakePsychoTicks);
        if (disguiseRoleId != null) {
            tag.putString("DisguiseRoleId", disguiseRoleId.toString());
        }
    }

    @Override
    public void reset() {
        fakePsychoTicks = 0;
        disguiseRoleId = null;
    }

    @Override
    public void clear() {
        this.reset();
    }
}
