package org.agmas.noellesroles.item;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.agmas.noellesroles.packet.PostmanC2SPacket;

/**
 * 传递盒物品
 *
 * 功能：
 * - 邮差专属物品，在商店以350金币购买
 * - 指针对准玩家并右键使用，打开传递界面
 * - 双方可以放入一样物品并交换
 *
 * 注意：实际的使用逻辑在客户端的 DeliveryBoxItemClient 中通过 Mixin 实现
 */
public class DeliveryBoxItem extends Item {
    
    public DeliveryBoxItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) {

            // 客户端：检查是否瞄准玩家
            MinecraftClient client = MinecraftClient.getInstance();
            HitResult hitResult = client.crosshairTarget;

            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) hitResult;
                Entity target = entityHit.getEntity();

                if (target instanceof PlayerEntity targetPlayer && !targetPlayer.equals(user)) {
                    // 瞄准了其他玩家，发送打开传递的网络包
                    // 服务端会处理打开界面的逻辑
                    ClientPlayNetworking.send(new PostmanC2SPacket(
                            PostmanC2SPacket.Action.OPEN_DELIVERY,
                            targetPlayer.getUuid()
                    ));

                    return TypedActionResult.success(stack, true);
                }
            }

            // 没有瞄准玩家
            user.sendMessage(
                    Text.translatable("message.noellesroles.postman.no_target")
                            .formatted(Formatting.RED),
                    true
            );
            return TypedActionResult.fail(stack);
        }
        return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
    }
    
    @Override
    public boolean hasGlint(ItemStack stack) {
        // 不添加附魔光效
        return false;
    }
}