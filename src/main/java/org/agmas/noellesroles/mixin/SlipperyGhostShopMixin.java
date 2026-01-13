package org.agmas.noellesroles.mixin;


import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin：处理滑头鬼的商店功能
 * 让滑头鬼可以使用专属商店购买物品
 */
@Mixin(PlayerShopComponent.class)
public abstract class SlipperyGhostShopMixin {
//
//    @Shadow
//    public int balance;
//
//    @Shadow
//    @Final
//    private Player player;
//
//    @Shadow
//    public abstract void sync();
//
//    /**
//     * 注入到 tryBuy 方法
//     * 如果玩家是滑头鬼，使用滑头鬼专属商店
//     */
//    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
//    private void slipperyGhost$tryBuy(int index, CallbackInfo ci) {
//        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
//
//        // 如果玩家是滑头鬼，使用滑头鬼商店
//        if (gameWorld.isRole(player, ModRoles.SLIPPERY_GHOST)) {
//            // 验证索引范围
//            if (index < 0 || index >= Noellesroles.SLIPPERY_GHOST_SHOP.size()) {
//                ci.cancel();
//                return;
//            }
//
//            ShopEntry entry = Noellesroles.SLIPPERY_GHOST_SHOP.get(index);
//
//            if (balance >= entry.price()) {
//                this.balance -= entry.price();
//                sync();
//
//                // 检查是否为关灯物品，如果是则直接触发关灯效果（不给物品）
//                if (entry.stack().is(TMMItems.BLACKOUT)) {
//                    // 使用原版的关灯方法
//                    PlayerShopComponent.useBlackout(player);
//                } else {
//                    // 其他物品直接给予
//                    player.addItem(entry.stack().copy());
//                }
//
//                // 播放购买成功音效
//                if (player instanceof ServerPlayer serverPlayer) {
//                    serverPlayer.connection.send(new ClientboundSoundPacket(
//                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.UI_SHOP_BUY),
//                        SoundSource.PLAYERS,
//                        player.getX(), player.getY(), player.getZ(),
//                        1.0F,
//                        0.9F + player.getRandom().nextFloat() * 0.2F,
//                        player.getRandom().nextLong()
//                    ));
//                }
//            } else {
//                // 购买失败
//                player.displayClientMessage(Component.literal("金币不足").withStyle(ChatFormatting.DARK_RED), true);
//
//                // 播放购买失败音效
//                if (player instanceof ServerPlayer serverPlayer) {
//                    serverPlayer.connection.send(new ClientboundSoundPacket(
//                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.UI_SHOP_BUY_FAIL),
//                        SoundSource.PLAYERS,
//                        player.getX(), player.getY(), player.getZ(),
//                        1.0F,
//                        0.9F + player.getRandom().nextFloat() * 0.2F,
//                        player.getRandom().nextLong()
//                    ));
//                }
//            }
//
//            // 取消原版购买逻辑
//            ci.cancel();
//        }
//    }
}