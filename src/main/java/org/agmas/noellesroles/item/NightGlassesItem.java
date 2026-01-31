package org.agmas.noellesroles.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class NightGlassesItem extends ArmorItem {
    public int tick = 0;

    public NightGlassesItem(Holder<ArmorMaterial> holder, Type type, Properties properties) {
        super(holder, type, properties);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
        if (entity instanceof Player pl) {
            ItemStack headItem = pl.getSlot(103).get();
            if (headItem.equals(itemStack)) {
                tick++;
                if (tick % 20 == 0) {
                    itemStack.consume(1, pl);
                    pl.addEffect(new MobEffectInstance(
                            MobEffects.NIGHT_VISION, // ID
                            30, // 持续时间（tick）
                            0, // 等级（0 = 速度 I）
                            false, // ambient（环境效果，如信标）
                            true, // showParticles（显示粒子）
                            false // showIcon（显示图标）
                    ));
                }
            }
        }
    }

}
