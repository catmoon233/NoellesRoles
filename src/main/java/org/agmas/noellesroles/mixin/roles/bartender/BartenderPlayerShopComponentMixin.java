package org.agmas.noellesroles.mixin.roles.bartender;

import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerShopComponent.class)
public abstract class BartenderPlayerShopComponentMixin {
    @Shadow public int balance;

    @Shadow @Final private PlayerEntity player;

    @Shadow public abstract void sync();

//    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
//    void bartenderBuy(int index, CallbackInfo ci) {
//        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
//        if (gameWorldComponent.isRole(player,Noellesroles.BARTENDER)) {
//            if (index == 0) {
//                if (balance >= 250) {
//                    this.balance -= 250;
//                    sync();
//                    player.giveItemStack(ModItems.DEFENSE_VIAL.getDefaultStack());
//                    PlayerEntity var6 = this.player;
//                    if (var6 instanceof ServerPlayerEntity) {
//                        ServerPlayerEntity player = (ServerPlayerEntity) var6;
//                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(TMMSounds.UI_SHOP_BUY), SoundCategory.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0F, 0.9F + this.player.getRandom().nextFloat() * 0.2F, player.getRandom().nextLong()));
//                    }
//                } else {
//                    this.player.sendMessage(Text.literal("购买失败").formatted(Formatting.DARK_RED), true);
//                    PlayerEntity var4 = this.player;
//                    if (var4 instanceof ServerPlayerEntity) {
//                        ServerPlayerEntity player = (ServerPlayerEntity) var4;
//                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(TMMSounds.UI_SHOP_BUY_FAIL), SoundCategory.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0F, 0.9F + this.player.getRandom().nextFloat() * 0.2F, player.getRandom().nextLong()));
//                    }
//                }
//            }
//            ci.cancel();
//        }
//    }

}
