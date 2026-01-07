package org.agmas.noellesroles.mixin.roles;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin：处理邮差的商店功能
 * 让邮差可以使用专属商店购买传递盒
 */
@Mixin(PlayerShopComponent.class)
public abstract class PostmanShopMixin {
    
    @Shadow
    public int balance;
    
    @Shadow
    @Final
    private Player player;
    
    @Shadow
    public abstract void sync();
    
//    /**
//     * 注入到 tryBuy 方法
//     * 如果玩家是邮差，使用邮差专属商店
//     */
//    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
//    private void postman$tryBuy(int index, CallbackInfo ci) {
//        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
//
//        // 如果玩家是邮差，使用邮差商店
//        if (gameWorld.isRole(player, ModRoles.POSTMAN)) {
//            // 验证索引范围
//            if (index < 0 || index >= Noellesroles.POSTMAN_SHOP.size()) {
//                ci.cancel();
//                return;
//            }
//
//            ShopEntry entry = Noellesroles.POSTMAN_SHOP.get(index);
//
//            if (balance >= entry.price()) {
//                this.balance -= entry.price();
//                sync();
//                // 直接给予物品
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