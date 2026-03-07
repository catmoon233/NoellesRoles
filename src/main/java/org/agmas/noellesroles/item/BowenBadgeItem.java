package org.agmas.noellesroles.item;

import org.agmas.noellesroles.Noellesroles;

import dev.doctor4t.trainmurdermystery.block.SmallDoorBlock;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BowenBadgeItem extends Item {

    public BowenBadgeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        player.startUsingItem(usedHand);
        // 蓄力时没有声音
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 3 * 20;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CROSSBOW;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player))
            return;
        if (level.isClientSide)
            return;

        // 只在冲刺旋转期间检测碰撞
        if (!player.isAutoSpinAttack())
            return;

        Vec3 playerPos = player.position();
        Vec3 movement = player.getDeltaMovement();

        // 以当前移动方向为碰撞检测朝向
        Vec3 dashDir = movement.multiply(1, 0, 1).normalize();
        Vec3 frontCenter = playerPos.add(dashDir.scale(0.8));
        AABB collisionBox = new AABB(frontCenter, frontCenter).inflate(0.6, 0.9, 0.6);

        for (var e : level.getEntities(player, collisionBox)) {
            if (!(e instanceof Player targetPlayer))
                continue;

            // 撞到目标：停止水平移动
            player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
            if (GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                GameFunctions.killPlayer(targetPlayer, true, player, Noellesroles.id("bowen"));
            }
            break;
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player))
            return itemStack;

        var holder = EnchantmentHelper
                .pickHighestLevel(itemStack, EnchantmentEffectComponents.TRIDENT_SOUND)
                .orElse(SoundEvents.TRIDENT_THROW);
        player.awardStat(Stats.ITEM_USED.get(this));

        float f = 1f;
        float g = player.getYRot();

        // 水平朝向向量
        float k = -Mth.sin(g * ((float) Math.PI / 180F));
        float m = Mth.cos(g * ((float) Math.PI / 180F));
        float horizLen = Mth.sqrt(k * k + m * m);
        float kNorm = k / horizLen;
        float mNorm = m / horizLen;

        // ── 击退周围的玩家（排除正前方碰撞目标）──
        Vec3 playerPos = player.position();
        AABB nearbyBox = new AABB(playerPos, playerPos).inflate(3.0, 1.5, 3.0);

        Vec3 dashFront = playerPos.add(kNorm * 2.5, 0, mNorm * 2.5);
        AABB collisionBox = new AABB(dashFront, dashFront).inflate(0.8, 1.0, 0.8);

        for (var entity : level.getEntities(player, nearbyBox)) {
            if (!(entity instanceof LivingEntity target))
                continue;
            if (collisionBox.intersects(target.getBoundingBox()))
                continue;

            Vec3 knockback = target.position()
                    .subtract(playerPos)
                    .multiply(1, 0, 1)
                    .normalize()
                    .scale(1.5);
            target.push(knockback.x, 0, knockback.z);
        }

        // ── 启动平飞冲刺 ──
        player.push(kNorm * f, 0.0, mNorm * f);
        player.startAutoSpinAttack(20, 8.0F, itemStack);
        if (player.onGround()) {
            player.move(MoverType.SELF, new Vec3(0.0, 1.2, 0.0));
        }

        level.playSound(null, player, holder.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        if (GameFunctions.isPlayerAliveAndSurvival(player)) {
            applyCooldownToItem(player, itemStack);
        }

        return itemStack;
    }

    // 攻击方式:
    // 初始获得波纹勋章，蓄力3s挥出一拳，击飞旁边的人，目标死亡,向前冲刺，有小脑惩罚,冷却60s
    private void applyCooldownToItem(Player player, ItemStack stack) {
        var cooldowns = player.getCooldowns();
        if (!stack.isEmpty() && !cooldowns.isOnCooldown(stack.getItem())) {
            cooldowns.addCooldown(stack.getItem(), 20 * 60);
        }
    }

    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof SmallDoorBlock) {
            return InteractionResult.PASS;
        } else {
            if (player != null) {
                context.getItemInHand().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }
            return super.useOn(context);
        }
    }
}
