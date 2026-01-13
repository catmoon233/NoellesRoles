package org.agmas.noellesroles.mixin.roles.bartender;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerShopComponent.class)
public abstract class BartenderPlayerShopComponentMixin {
    @Shadow public int balance;

    @Shadow @Final private Player player;

    @Shadow public abstract void sync();

//    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
//    void bartenderBuy(int index, CallbackInfo ci) {
//        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
//        if (gameWorldComponent.isRole(player, ModRoles.BARTENDER)) {
//            if (index == 0) {
//                if (balance >= 200) {
//                    this.balance -= 200;
//                    sync();
//                    player.addItem(ModItems.DEFENSE_VIAL.getDefaultInstance());
//                    Player var6 = this.player;
//                    if (var6 instanceof ServerPlayer) {
//                        ServerPlayer player = (ServerPlayer) var6;
//                        player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.UI_SHOP_BUY), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0F, 0.9F + this.player.getRandom().nextFloat() * 0.2F, player.getRandom().nextLong()));
//                    }
//                } else {
//                    this.player.displayClientMessage(Component.literal("购买失败").withStyle(ChatFormatting.DARK_RED), true);
//                    Player var4 = this.player;
//                    if (var4 instanceof ServerPlayer) {
//                        ServerPlayer player = (ServerPlayer) var4;
//                        player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.UI_SHOP_BUY_FAIL), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0F, 0.9F + this.player.getRandom().nextFloat() * 0.2F, player.getRandom().nextLong()));
//                    }
//                }
//            }
//            ci.cancel();
//        }
//    }

}
