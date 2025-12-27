package org.agmas.noellesroles.mixin.roles.thief;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
    private PlayerEntity player;
    @Shadow public abstract void sync();

    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
    void thiefTryBuy(int index, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
        if (!gameWorldComponent.isRole(player, ModRoles.THIEF)) {
            return;
        }

        ThiefPlayerComponent thiefComponent = ThiefPlayerComponent.KEY.get(player);

        if (index == 0) {
            if (balance >= thiefComponent.blackoutPrice) {
                this.balance -= thiefComponent.blackoutPrice;
                sync();

                if (player instanceof ServerPlayerEntity serverPlayer) {
                    int durationTicks = NoellesRolesConfig.HANDLER.instance().thiefBlackoutDuration * 20;
                    serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, durationTicks, 0, true, false, true));
                    
                    serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(
                        Registries.SOUND_EVENT.getEntry(TMMSounds.UI_SHOP_BUY),
                        net.minecraft.sound.SoundCategory.PLAYERS,
                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        1.0F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F,
                        serverPlayer.getRandom().nextLong()
                    ));
                    
                    thiefComponent.activateBlackout();
                    thiefComponent.upgradeBlackoutPrice();
                }
                
                ci.cancel();
            } else {
                player.sendMessage(Text.translatable("message.noellesroles.insufficient_funds").formatted(Formatting.DARK_RED), true);
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(
                        Registries.SOUND_EVENT.getEntry(TMMSounds.UI_SHOP_BUY_FAIL),
                        net.minecraft.sound.SoundCategory.PLAYERS,
                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        1.0F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F,
                        serverPlayer.getRandom().nextLong()
                    ));
                }
                ci.cancel();
            }
        }
        else if (index == 1) {
            if (balance >= 200) {
                this.balance -= 200;
                sync();
                player.giveItemStack(ModItems.MASTER_KEY.getDefaultStack());
                
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(
                        Registries.SOUND_EVENT.getEntry(TMMSounds.UI_SHOP_BUY),
                        net.minecraft.sound.SoundCategory.PLAYERS,
                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        1.0F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F,
                        serverPlayer.getRandom().nextLong()
                    ));
                }
                
                ci.cancel();
            } else {
                player.sendMessage(Text.translatable("message.noellesroles.insufficient_funds").formatted(Formatting.DARK_RED), true);
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(
                        Registries.SOUND_EVENT.getEntry(TMMSounds.UI_SHOP_BUY_FAIL),
                        net.minecraft.sound.SoundCategory.PLAYERS,
                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        1.0F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F,
                        serverPlayer.getRandom().nextLong()
                    ));
                }
                ci.cancel();
            }
        }
        else if (index == 2) {
            if (balance >= 1000) {
                this.balance -= 1000;
                sync();
                
                thiefComponent.purchaseThiefsHonor();
                
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(
                        Registries.SOUND_EVENT.getEntry(TMMSounds.UI_SHOP_BUY),
                        net.minecraft.sound.SoundCategory.PLAYERS,
                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        1.0F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F,
                        serverPlayer.getRandom().nextLong()
                    ));
                    serverPlayer.sendMessage(Text.translatable("message.thief.honor_purchased").formatted(Formatting.GREEN), true);
                }
                
                ci.cancel();
            } else {
                player.sendMessage(Text.translatable("message.noellesroles.insufficient_funds").formatted(Formatting.DARK_RED), true);
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(
                        Registries.SOUND_EVENT.getEntry(TMMSounds.UI_SHOP_BUY_FAIL),
                        net.minecraft.sound.SoundCategory.PLAYERS,
                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        1.0F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F,
                        serverPlayer.getRandom().nextLong()
                    ));
                }
                ci.cancel();
            }
        }
    }
}