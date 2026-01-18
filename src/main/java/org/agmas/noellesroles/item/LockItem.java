package org.agmas.noellesroles.item;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.block_entity.DoorBlockEntity;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMEntities;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.util.AdventureUsable;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.agmas.noellesroles.ModEntities;
import org.agmas.noellesroles.entity.LockEntity;

/**
 * 门锁
 * - 可以锁门来影响撬锁器的功能
 * - 可以被钥匙和万能钥匙打开，对撬棍无效
 * - 锁的强度由长度决定
 *
 * TODO: 一扇门是否可以放置多个锁（暂时允许多个锁）
 * TODO: 锁门的功能实现
 */
public class LockItem extends Item implements AdventureUsable {
    public LockItem(int length, Properties properties) {
        super(properties);
        this.length = length;
    }

    /**
     * 计算锁实体的位置
     * @param context 上下文
     * @param doorFacing 门的朝向（如果是其它方块理论上也可以计算)
     * @return 锁的坐标
     */
    public Vec3 getLockEntityPos(UseOnContext context, DoorBlockEntity door){
        double x = context.getClickedPos().getX() + 0.5;
        // 确保锁位于上半部分
        double y = door.getBlockPos().above().getY();
        double z = context.getClickedPos().getZ() + 0.5;
        switch (door.getFacing())
        {
        case EAST:
        case WEST:
            x = context.getClickLocation().x;
            break;
        case SOUTH:
        case NORTH:
            z = context.getClickLocation().z;
            break;
        }
        return new Vec3(x, y, z);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level world = context.getLevel();
        BlockEntity entity = world.getBlockEntity(context.getClickedPos());
        if (!(entity instanceof DoorBlockEntity))
            entity = world.getBlockEntity(context.getClickedPos().below());
        Player player = context.getPlayer();
        if (entity instanceof DoorBlockEntity door && !door.isBlasted() && player != null) {
            // 判定玩家点击的方向：只运行在门的正反面点击
            Direction clickedFace = context.getClickedFace();
            Direction doorFacing = door.getFacing();
            if(clickedFace != doorFacing && clickedFace != doorFacing.getOpposite())
            {
                return InteractionResult.PASS;
            }

            world.playSound(null, context.getClickedPos(), TMMSounds.BLOCK_DOOR_LOCKED, SoundSource.BLOCKS, 1f, 1f);
            player.swing(InteractionHand.MAIN_HAND, true);

            // 将锁添加到世界中
            if(!world.isClientSide)
            {
                LockEntity lockEntity = new LockEntity(ModEntities.LOCK_ENTITY, world);
                lockEntity.setPos(getLockEntityPos(context, door));
                lockEntity.setXRot(0.f);
                lockEntity.setYRot(door.getFacing().toYRot());
                world.addFreshEntity(lockEntity);
                return InteractionResult.SUCCESS;
            }

            //回放记录
//            if (!player.isCreative()) {
//                if (TMM.REPLAY_MANAGER != null) {
//                    TMM.REPLAY_MANAGER.recordItemUse(player.getUUID(), BuiltInRegistries.ITEM.getKey(this));
//                }
//                player.getCooldowns().addCooldown(this, GameConstants.ITEM_COOLDOWNS.get(this));
//            }

        }
        return super.useOn(context);
    }


    private final int length;
}
