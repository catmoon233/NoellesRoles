package org.agmas.noellesroles.mixin.roles.awesome_binglus;

import com.mojang.serialization.Codec;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerNoteComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.entity.NoteEntity;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMEntities;
import dev.doctor4t.trainmurdermystery.item.NoteItem;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NoteItem.class)
public abstract class NoteMixin extends Item {
    public NoteMixin(Properties properties) {
        super(properties);
    }

//    private static HitResult getTarget(Player user) {
//        return ProjectileUtil.getHitResultOnViewVector(user, entity -> entity instanceof Player player && GameFunctions.isPlayerAliveAndSurvival(player), 4f);
//    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
        if (player instanceof ServerPlayer serverPlayer){
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(serverPlayer.level());
            if (gameWorld.isRole(player, ModRoles.AWESOME_BINGLUS)) {
                final var playerShopComponent = PlayerShopComponent.KEY.get(serverPlayer);
                if (playerShopComponent.balance >= 75){
                    playerShopComponent.setBalance(playerShopComponent.balance - 75);
                    if (player != null && !player.isShiftKeyDown()) {
                        PlayerNoteComponent component = (PlayerNoteComponent)PlayerNoteComponent.KEY.get(player);
                        if (!component.written) {
                            player.displayClientMessage(Component.literal("我应该先写下点东西").withColor(Mth.hsvToRgb(0.0F, 1.0F, 0.6F)), true);
                            return InteractionResult.PASS;
                        } else {
                            Level world = player.level();
                            if (world.isClientSide) {
                                return InteractionResult.PASS;
                            } else {
                                NoteEntity note = (NoteEntity) TMMEntities.NOTE.create(world);
                                note.setAttached(ModRoles.ENTITY_NOTE_MAKER, livingEntity.getUUID().toString());
                                if (note == null) {
                                    return InteractionResult.PASS;
                                } else {
                                    note.setYRot(livingEntity.getYHeadRot());
                                    note.setPos(livingEntity.getX(), livingEntity.getY()+1, livingEntity.getZ());

                                    note.setDirection(Direction.EAST);
                                    note.setLines(component.text);
                                    player.displayClientMessage(Component.literal("你花费75\uE781将一张便签贴到了"+livingEntity.getName().getString()+"的背上").withColor(Mth.hsvToRgb(0.0F, 1.0F, 0.6F)), true);

                                    world.addFreshEntity(note);
                                    if (!player.isCreative()) {
                                        if (TMM.REPLAY_MANAGER != null) {
                                            TMM.REPLAY_MANAGER.recordItemUse(player.getUUID(), BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
                                        }

                                        itemStack.shrink(1);
                                    }

                                    return InteractionResult.SUCCESS;
                                }
                            }
                        }
                    }
                }else {
                    player.displayClientMessage(Component.literal("你没有足够的钱 - 需要75\uE781来将便签放到人身上").withColor(Mth.hsvToRgb(0.0F, 1.0F, 0.6F)), true);
                }
            }
        }
        return super.interactLivingEntity(itemStack, player, livingEntity, interactionHand);
    }
}
