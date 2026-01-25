package org.agmas.noellesroles.mixin.roles.gambler;
import dev.doctor4t.trainmurdermystery.api.Role;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(Role.class)
public  class MixinGamblerRole {

	public Optional<SoundEvent> getBucketEmptySound$myMod() {
		// 这是如何获取默认声音的，从 BucketItem 类中复制。
		return Optional.of( SoundEvents.BUCKET_FILL_LAVA );
	}
}