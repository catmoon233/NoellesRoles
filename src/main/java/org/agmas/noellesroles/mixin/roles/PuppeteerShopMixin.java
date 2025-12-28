package org.agmas.noellesroles.mixin.roles;


import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.PuppeteerPlayerComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin：阻止傀儡师本体状态使用商店
 * 傀儡师在阶段二本体状态（非操控假人时）不能使用商店
 * 只有操控假人时才能购买
 */
@Mixin(value = PlayerShopComponent.class, priority = 900)
public abstract class PuppeteerShopMixin {
    
    @Shadow
    @Final
    private PlayerEntity player;
    
    /**
     * 注入到 tryBuy 方法的最开始
     * 如果玩家是傀儡师本体状态（阶段二且不在操控假人），阻止购买
     */
    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
    private void puppeteer$preventShopAccess(int index, CallbackInfo ci) {
        PuppeteerPlayerComponent puppeteerComp = ModComponents.PUPPETEER.get(player);
        
        // 如果玩家是傀儡师阶段二，且不在操控假人状态，阻止使用商店
        if (puppeteerComp.isPuppeteerMarked && puppeteerComp.phase == 2 && !puppeteerComp.isControllingPuppet) {
            // 发送提示消息
            player.sendMessage(Text.translatable("message.rices-role-rhapsody.puppeteer.shop_blocked"), true);
            
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
            
            // 取消购买
            ci.cancel();
        }
    }
}