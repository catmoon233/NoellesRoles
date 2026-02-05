package org.agmas.noellesroles.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.agmas.noellesroles.component.DefibrillatorComponent;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.roles.coroner.BodyDeathReasonComponent;
import org.agmas.noellesroles.utils.RoleUtils;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;

public class DefibrillatorItem extends Item {
    public DefibrillatorItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeCharged) {
        if (!level.isClientSide && user instanceof Player player) {
            if (this.getUseDuration(stack, user) - timeCharged >= 10) {
                net.minecraft.world.phys.HitResult hitResult = getDefibrillatorTarget(player);

                if (hitResult instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
                    if (entityHitResult.getEntity() instanceof PlayerBodyEntity) {
                        PlayerBodyEntity body = (PlayerBodyEntity) entityHitResult.getEntity();
                        if (body.tickCount > 90 * 20) {
                            player.displayClientMessage(
                                    Component.translatable("message.noellesroles.defibrillator.too_late"),
                                    true);
                            return;
                        }

                        // 通过UUID查找玩家，而不是通过名字
                        java.util.UUID playerUuid = body.getPlayerUuid();
                        net.minecraft.server.level.ServerPlayer target = player.getServer().getPlayerList().getPlayer(playerUuid);

                        if (target != null) {
                            target.teleportTo(body.getX(), body.getY(), body.getZ());
                            target.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);
                            target.setHealth(target.getMaxHealth());
                            target.removeAllEffects();

                            // 获取尸体的死亡原因组件，读取原始职业
                            BodyDeathReasonComponent bodyComponent = BodyDeathReasonComponent.KEY.get(body);
                            net.minecraft.resources.ResourceLocation originalRoleId = bodyComponent.playerRole;
                            Role originalRole = TMMRoles.ROLES.get(originalRoleId);

                            // 如果找到原始职业，恢复它
                            if (originalRole != null) {
                                RoleUtils.changeRole(target, originalRole, false);
                            }

                            DefibrillatorComponent component = ModComponents.DEFIBRILLATOR.get(target);
                            component.reset();

                            body.discard();

                            player.displayClientMessage(
                                    Component.translatable("message.noellesroles.defibrillator.revived", target.getName()),
                                    true);
                            target.displayClientMessage(
                                    Component.translatable("message.noellesroles.defibrillator.you_revived"), true);

                            if (!player.isCreative()) {
                                stack.shrink(1);
                            }
                        } else {
                            player.displayClientMessage(
                                    Component.translatable("message.noellesroles.defibrillator.player_not_found"),
                                    true);
                        }
                    }
                }
            }
        }
    }

    public static net.minecraft.world.phys.HitResult getDefibrillatorTarget(Player user) {
        return net.minecraft.world.entity.projectile.ProjectileUtil.getHitResultOnViewVector(user, (entity) -> {
            return entity instanceof dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
        }, 3.0F);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }
}