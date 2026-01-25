package org.agmas.noellesroles.mixin.roles.gambler;
import dev.doctor4t.trainmurdermystery.api.Role;

@Mixin(Role.class)
public class MixinGamblerRole implements Role {
	@Override
	public Optional<SoundEvent> getBucketEmptySound$myMod() {
		// 这是如何获取默认声音的，从 BucketItem 类中复制。
		return Optional.of(((FlowableFluid) (Object) this).isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY);
	}
}