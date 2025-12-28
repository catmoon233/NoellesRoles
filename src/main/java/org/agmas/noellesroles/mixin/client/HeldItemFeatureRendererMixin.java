package org.agmas.noellesroles.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import org.agmas.noellesroles.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.UUID;
@Mixin({HeldItemFeatureRenderer.class})
public class HeldItemFeatureRendererMixin {
    @WrapOperation(
            method = {"render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"
            )}
    )
    public ItemStack tmm$hideNoteAndRenderPsychosisItems(LivingEntity instance, Operation<ItemStack> original) {
        ItemStack ret = (ItemStack)original.call(instance);
        if (ret.isOf(ModItems.ALARM_TRAP )||  ret.isOf(ModItems.CONSPIRACY_PAGE ) || ret.isOf(ModItems.REINFORCEMENT)) {
            ret = ItemStack.EMPTY;
        }



        return ret;
    }
}