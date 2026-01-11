package org.agmas.noellesroles.mixin.roles;


import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Mixin：处理工程师的商店功能
 * 让工程师可以使用专属商店购买物品
 */
@Mixin(PlayerShopComponent.class)
public abstract class EngineerShopMixin {
    
    @Shadow
    public int balance;
    
    @Shadow
    @Final
    private Player player;
    
    @Shadow
    public abstract void sync();
    
    /**
     * 注入到 tryBuy 方法
     * 如果玩家是工程师，使用工程师专属商店
     */
//    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
//    private void engineer$tryBuy(int index, CallbackInfo ci) {
//        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
//
//        // 如果玩家是工程师，使用工程师商店
//        if (gameWorld.isRole(player, ModRoles.ENGINEER)) {
//            // 验证索引范围
//            if (index < 0 || index >= Noellesroles.ENGINEER_SHOP.size()) {
//                ci.cancel();
//                return;
//            }
//
//            ShopEntry entry = Noellesroles.ENGINEER_SHOP.get(index);
//
//            if (balance >= entry.price()) {
//                this.balance -= entry.price();
//                sync();
//                // 直接给予物品，而不是调用 entry.onBuy
//                player.addItem(entry.stack().copy());
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