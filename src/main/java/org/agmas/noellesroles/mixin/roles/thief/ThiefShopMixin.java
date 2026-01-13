package org.agmas.noellesroles.mixin.roles.thief;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.thief.ThiefPlayerComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerShopComponent.class)
public abstract class ThiefShopMixin {
    @Shadow public int balance;
    @Shadow @Final
    private Player player;
    @Shadow public abstract void sync();

//    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
//    void thiefTryBuy(int index, CallbackInfo ci) {
//        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
//        if (!gameWorldComponent.isRole(player, ModRoles.THIEF)) {
//            return;
//        }
//
//        ThiefPlayerComponent thiefComponent = ThiefPlayerComponent.KEY.get(player);
//
//        if (index == 0) {
//            if (balance >= thiefComponent.blackoutPrice) {
//                this.balance -= thiefComponent.blackoutPrice;
//                sync();
//
//                if (player instanceof ServerPlayer serverPlayer) {
//                    int durationTicks = NoellesRolesConfig.HANDLER.instance().thiefBlackoutDuration * 20;
//                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, durationTicks, 0, true, false, true));
//
//                    serverPlayer.connection.send(new ClientboundSoundPacket(
//                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.UI_SHOP_BUY),
//                        net.minecraft.sounds.SoundSource.PLAYERS,
//                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
//                        1.0F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F,
//                        serverPlayer.getRandom().nextLong()
//                    ));
//
//                    thiefComponent.activateBlackout();
//                    thiefComponent.upgradeBlackoutPrice();
//                }
//
//                ci.cancel();
//            } else {
//                player.displayClientMessage(Component.translatable("message.noellesroles.insufficient_funds").withStyle(ChatFormatting.DARK_RED), true);
//                if (player instanceof ServerPlayer serverPlayer) {
//                    serverPlayer.connection.send(new ClientboundSoundPacket(
//                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.UI_SHOP_BUY_FAIL),
//                        net.minecraft.sounds.SoundSource.PLAYERS,
//                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
//                        1.0F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F,
//                        serverPlayer.getRandom().nextLong()
//                    ));
//                }
//                ci.cancel();
//            }
//        }
//        else if (index == 1) {
//            if (balance >= 200) {
//                this.balance -= 200;
//                sync();
//                player.addItem(ModItems.MASTER_KEY.getDefaultInstance());
//
//                if (player instanceof ServerPlayer serverPlayer) {
//                    serverPlayer.connection.send(new ClientboundSoundPacket(
//                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.UI_SHOP_BUY),
//                        net.minecraft.sounds.SoundSource.PLAYERS,
//                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
//                        1.0F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F,
//                        serverPlayer.getRandom().nextLong()
//                    ));
//                }
//
//                ci.cancel();
//            } else {
//                player.displayClientMessage(Component.translatable("message.noellesroles.insufficient_funds").withStyle(ChatFormatting.DARK_RED), true);
//                if (player instanceof ServerPlayer serverPlayer) {
//                    serverPlayer.connection.send(new ClientboundSoundPacket(
//                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.UI_SHOP_BUY_FAIL),
//                        net.minecraft.sounds.SoundSource.PLAYERS,
//                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
//                        1.0F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F,
//                        serverPlayer.getRandom().nextLong()
//                    ));
//                }
//                ci.cancel();
//            }
//        }
//        else if (index == 2) {
//            if (balance >= 1000) {
//                this.balance -= 1000;
//                sync();
//
//                thiefComponent.purchaseThiefsHonor();
//
//                if (player instanceof ServerPlayer serverPlayer) {
//                    serverPlayer.connection.send(new ClientboundSoundPacket(
//                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.UI_SHOP_BUY),
//                        net.minecraft.sounds.SoundSource.PLAYERS,
//                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
//                        1.0F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F,
//                        serverPlayer.getRandom().nextLong()
//                    ));
//                    serverPlayer.displayClientMessage(Component.translatable("message.thief.honor_purchased").withStyle(ChatFormatting.GREEN), true);
//                }
//
//                ci.cancel();
//            } else {
//                player.displayClientMessage(Component.translatable("message.noellesroles.insufficient_funds").withStyle(ChatFormatting.DARK_RED), true);
//                if (player instanceof ServerPlayer serverPlayer) {
//                    serverPlayer.connection.send(new ClientboundSoundPacket(
//                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.UI_SHOP_BUY_FAIL),
//                        net.minecraft.sounds.SoundSource.PLAYERS,
//                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
//                        1.0F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F,
//                        serverPlayer.getRandom().nextLong()
//                    ));
//                }
//                ci.cancel();
//            }
//        }
//    }
}