package org.agmas.noellesroles.item;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;

public class BombItem extends Item {
    public static final String TIMER_KEY = "bomb_timer";
    public static final int MAX_TIMER = 400; // 20 seconds * 20 ticks

    public BombItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide)
            return;
        if (!(entity instanceof ServerPlayer player))
            return;

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (!tag.contains(TIMER_KEY)) {
            tag.putInt(TIMER_KEY, MAX_TIMER);
        }

        int timer = tag.getInt(TIMER_KEY);
        if (timer > 0) {
            timer--;
            tag.putInt(TIMER_KEY, timer);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            // Play ticking sound every second (silent for first 5 seconds)
            if (timer % 20 == 0 && (MAX_TIMER - timer) > 100) {
                level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS,
                        1.0f, 1.0f + (MAX_TIMER - timer) / 200f);
            }

            // Show timer in action bar
            // player.displayClientMessage(Component.literal("Bomb: " + (timer / 20) + "s"), true);
        } else {
            // Explode
            explode(player, stack);
        }
    }

    private void explode(ServerPlayer player, ItemStack stack) {
        stack.shrink(1);
        player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS,
                2.0f, 1.0f);
        ((ServerLevel) player.level()).sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY(),
                player.getZ(), 1, 0, 0, 0, 0);
        GameFunctions.killPlayer(player, true, null, Noellesroles.id("bomb_death"));
        ServerLevel serverLevel = player.serverLevel();
        serverLevel.players().forEach(
                target -> {
                    if (GameWorldComponent.KEY.get(serverLevel).isRole(target, ModRoles.BOMBER)){
                        PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(target);
                        playerShopComponent.setBalance(35 + playerShopComponent.balance);
                    }
                }
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide)
            return InteractionResultHolder.pass(stack);

        // Raycast to find target player
        double reachDistance = 5.0; // 5 blocks reach
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookVector = player.getViewVector(1.0F);
        Vec3 endPosition = eyePosition.add(lookVector.scale(reachDistance));
        AABB searchBox = player.getBoundingBox().expandTowards(lookVector.scale(reachDistance)).inflate(1.0D);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(player, eyePosition, endPosition, searchBox,
                (entity) -> entity instanceof Player && !entity.isSpectator(), reachDistance * reachDistance);

        if (hitResult != null && hitResult.getEntity() instanceof ServerPlayer target) {
            // Transfer bomb
            ItemStack newStack = stack.copy();
            stack.shrink(1);

            if (!target.getInventory().add(newStack)) {
                target.drop(newStack, false);
            }

            // Play sound
            level.playSound(null, player.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);

            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.pass(stack);
    }
}