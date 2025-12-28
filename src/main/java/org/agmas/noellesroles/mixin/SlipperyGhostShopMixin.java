package org.agmas.noellesroles.mixin;


import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin：处理滑头鬼的商店功能
 * 让滑头鬼可以使用专属商店购买物品
 */
@Mixin(PlayerShopComponent.class)
public abstract class SlipperyGhostShopMixin {
    
    @Shadow
    public int balance;
    
    @Shadow
    @Final
    private PlayerEntity player;
    
    @Shadow
    public abstract void sync();
    
    /**
     * 注入到 tryBuy 方法
     * 如果玩家是滑头鬼，使用滑头鬼专属商店
     */
    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
    private void slipperyGhost$tryBuy(int index, CallbackInfo ci) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        
        // 如果玩家是滑头鬼，使用滑头鬼商店
        if (gameWorld.isRole(player, ModRoles.SLIPPERY_GHOST)) {
            // 验证索引范围
            if (index < 0 || index >= Noellesroles.SLIPPERY_GHOST_SHOP.size()) {
                ci.cancel();
                return;
            }
            
            ShopEntry entry = Noellesroles.SLIPPERY_GHOST_SHOP.get(index);
            
            if (balance >= entry.price()) {
                this.balance -= entry.price();
                sync();
                
                // 检查是否为关灯物品，如果是则直接触发关灯效果（不给物品）
                if (entry.stack().isOf(TMMItems.BLACKOUT)) {
                    // 使用原版的关灯方法
                    PlayerShopComponent.useBlackout(player);
                } else {
                    // 其他物品直接给予
                    player.giveItemStack(entry.stack().copy());
                }
                
                // 播放购买成功音效
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(
                        Registries.SOUND_EVENT.getEntry(TMMSounds.UI_SHOP_BUY),
                        SoundCategory.PLAYERS,
                        player.getX(), player.getY(), player.getZ(),
                        1.0F,
                        0.9F + player.getRandom().nextFloat() * 0.2F,
                        player.getRandom().nextLong()
                    ));
                }
            } else {
                // 购买失败
                player.sendMessage(Text.literal("金币不足").formatted(Formatting.DARK_RED), true);
                
                // 播放购买失败音效
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(
                        Registries.SOUND_EVENT.getEntry(TMMSounds.UI_SHOP_BUY_FAIL),
                        SoundCategory.PLAYERS,
                        player.getX(), player.getY(), player.getZ(),
                        1.0F,
                        0.9F + player.getRandom().nextFloat() * 0.2F,
                        player.getRandom().nextLong()
                    ));
                }
            }
            
            // 取消原版购买逻辑
            ci.cancel();
        }
    }
}