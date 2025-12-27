package org.agmas.noellesroles.mixin.client;

import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.PuppeteerPlayerComponent;
import org.agmas.noellesroles.component.StalkerPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.Set;

/**
 * 杀手本能 Mixin
 *
 * 处理以下功能：
 * 1. 跟踪者：杀手透视时显示跟踪者颜色（跟踪者本身是杀手，无需额外处理 isKiller）
 * 2. 爱慕者：类似小丑，能使用本能侦查，也能被杀手本能侦查到
 * 3. 傀儡师：操控假人时可以使用本能，显示渐变色
 * 4. 杀手本能颜色改为渐变色效果
 */
@Mixin(TMMClient.class)
public class StalkerInstinctMixin {
    
    @Shadow
    public static KeyBinding instinctKeybind;
    
    // 渐变色配置 - 从红色到橙色到黄色循环
    @Unique
    private static final int[] GRADIENT_COLORS = {
        new Color(255, 0, 0).getRGB(),      // 红色
        new Color(255, 85, 0).getRGB(),     // 橙红
        new Color(255, 170, 0).getRGB(),    // 橙色
        new Color(255, 255, 0).getRGB(),    // 黄色
        new Color(255, 170, 0).getRGB(),    // 橙色
        new Color(255, 85, 0).getRGB(),     // 橙红
    };
    
    // 渐变周期（tick）
    @Unique
    private static final int GRADIENT_CYCLE = 60; // 3秒一个周期
    
    // 定义杀手透视时显示绿色的乘客角色列表
    private static final int INNOCENT_GREEN = new Color(0, 255, 0).getRGB();
    private static final Set<Role> INNOCENT_ROLES_FOR_KILLER_INSTINCT = Set.of(
        ModRoles.AVENGER,
        ModRoles.TELEGRAPHER,
        ModRoles.ENGINEER,
        ModRoles.BOXER,
        ModRoles.POSTMAN,
        ModRoles.DETECTIVE,
        ModRoles.ATHLETE,
        ModRoles.STAR,
        ModRoles.VETERAN,
        ModRoles.SINGER,
        ModRoles.PSYCHOLOGIST
    );
    
    /**
     * 获取渐变颜色
     * @param tickOffset 每个实体的偏移量，使不同实体颜色略有不同
     * @return 当前渐变颜色
     */
    @Unique
    private static int getGradientColor(int tickOffset) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return GRADIENT_COLORS[0];
        
        long worldTime = client.world.getTime();
        int cyclePosition = (int) ((worldTime + tickOffset) % GRADIENT_CYCLE);
        
        // 计算在颜色数组中的位置
        float progress = (float) cyclePosition / GRADIENT_CYCLE * GRADIENT_COLORS.length;
        int colorIndex = (int) progress;
        float blend = progress - colorIndex;
        
        // 获取当前颜色和下一个颜色
        int currentColor = GRADIENT_COLORS[colorIndex % GRADIENT_COLORS.length];
        int nextColor = GRADIENT_COLORS[(colorIndex + 1) % GRADIENT_COLORS.length];
        
        // 混合两个颜色
        return blendColors(currentColor, nextColor, blend);
    }
    
    /**
     * 混合两个颜色
     */
    @Unique
    private static int blendColors(int color1, int color2, float blend) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        
        int r = (int) (r1 + (r2 - r1) * blend);
        int g = (int) (g1 + (g2 - g1) * blend);
        int b = (int) (b1 + (b2 - b1) * blend);
        
        return (r << 16) | (g << 8) | b;
    }
    
    /**
     * 让爱慕者和傀儡师操控假人时可以使用杀手本能（类似小丑）
     */
    @Inject(method = "isInstinctEnabled", at = @At("HEAD"), cancellable = true)
    private static void admirerAndPuppeteerCanUseInstinct(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.player.getWorld());
        
        // 爱慕者可以使用本能侦查（类似小丑）
        if (gameWorld.isRole(client.player, ModRoles.ADMIRER)) {
            if (instinctKeybind.isPressed()) {
                cir.setReturnValue(true);
                cir.cancel();
                return;
            }
        }
        
        // 傀儡师操控假人时可以使用本能侦查
        // 注意：操控假人时角色会临时变成其他杀手，所以需要检查组件状态而不是角色
        PuppeteerPlayerComponent puppeteerComp = ModComponents.PUPPETEER.get(client.player);
        if (puppeteerComp.isControllingPuppet && instinctKeybind.isPressed()) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
    
    /**
     * 处理杀手本能高亮颜色
     * 1. 跟踪者显示跟踪者颜色
     * 2. 爱慕者可以被杀手透视，也可以透视其他人
     */
    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void customInstinctHighlight(Entity target, CallbackInfoReturnable<Integer> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        
        if (!(target instanceof PlayerEntity targetPlayer)) return;
        if (targetPlayer.isSpectator()) return;
        
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.player.getWorld());
        
        // 只有在杀手本能激活时才处理
        if (!TMMClient.isInstinctEnabled()) return;
        
        // 计算实体偏移量（使不同玩家渐变稍有不同）
        int entityOffset = targetPlayer.getId() * 7;
        
        // =============== 跟踪者处理 ===============
        // 杀手透视跟踪者时，显示渐变色
        StalkerPlayerComponent stalkerComp = StalkerPlayerComponent.KEY.get(targetPlayer);
        if (stalkerComp.isStalkerMarked && stalkerComp.phase > 0) {
            if (TMMClient.isKiller() && TMMClient.isPlayerAliveAndInSurvival()) {
                // 使用渐变色
                cir.setReturnValue(getGradientColor(entityOffset));
                cir.cancel();
                return;
            }
        }
        
        // =============== 傀儡师处理 ===============
        // 杀手透视傀儡师时（阶段一和阶段二），显示渐变色
        PuppeteerPlayerComponent puppeteerComp = ModComponents.PUPPETEER.get(targetPlayer);
        if (puppeteerComp.isPuppeteerMarked && puppeteerComp.phase >= 1) {
            if (TMMClient.isKiller() && TMMClient.isPlayerAliveAndInSurvival()) {
                cir.setReturnValue(getGradientColor(entityOffset + 10));
                cir.cancel();
                return;
            }
        }
        
        // 傀儡师操控假人时透视其他人，显示渐变色
        // 注意：操控假人时角色会临时变成其他杀手，所以需要检查组件状态而不是角色
        PuppeteerPlayerComponent selfPuppeteerComp = ModComponents.PUPPETEER.get(client.player);
        if (selfPuppeteerComp.isControllingPuppet && TMMClient.isPlayerAliveAndInSurvival()) {
            cir.setReturnValue(getGradientColor(entityOffset));
            cir.cancel();
            return;
        }
        
        // =============== 爱慕者处理 ===============
        // 1. 杀手透视爱慕者时，显示爱慕者颜色（类似小丑）
        if (gameWorld.isRole(targetPlayer, ModRoles.ADMIRER)) {
            if (TMMClient.isKiller() && TMMClient.isPlayerAliveAndInSurvival()) {
                cir.setReturnValue(ModRoles.ADMIRER.color());
                cir.cancel();
                return;
            }
        }
        
        // 2. 爱慕者透视其他人时，显示粉色（类似小丑）
        if (gameWorld.isRole(client.player, ModRoles.ADMIRER) && TMMClient.isPlayerAliveAndInSurvival()) {
            cir.setReturnValue(Color.PINK.getRGB());
            cir.cancel();
            return;
        }
        
        // =============== 乘客角色透视处理 ===============
        // 杀手透视这些乘客角色时，显示渐变绿色
        if (TMMClient.isKiller() && TMMClient.isPlayerAliveAndInSurvival()) {
            for (Role role : INNOCENT_ROLES_FOR_KILLER_INSTINCT) {
                if (gameWorld.isRole(targetPlayer, role)) {
                    // 杀手看乘客使用渐变色
                    cir.setReturnValue(getGradientColor(entityOffset));
                    cir.cancel();
                    return;
                }
            }
        }
    }
}