package org.agmas.noellesroles.client.mixin.executioner;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.ExecutionerPlayerWidget;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.executioner.ExecutionerPlayerComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Executioner角色选择目标的UI界面Mixin
 * 当打开背包界面时，如果是Executioner且未选择目标，显示可选择的平民玩家
 */
@Mixin(LimitedInventoryScreen.class)
public abstract class ExecutionerScreenMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow @Final public ClientPlayerEntity player;

    public ExecutionerScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    void addExecutionerTargetSelection(CallbackInfo ci) {
        // 检查是否启用了手动选择目标功能
        if (!NoellesRolesConfig.HANDLER.instance().executionerCanSelectTarget) {
            return; // 如果未启用，则不显示选择界面
        }
        
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        
        // 检查是否是Executioner角色
        if (gameWorldComponent.isRole(player, Noellesroles.EXECUTIONER)) {
            ExecutionerPlayerComponent executionerComponent = ExecutionerPlayerComponent.KEY.get(player);
            
            // 只有在未选择目标时才显示选择界面
            if (!executionerComponent.targetSelected) {
                List<AbstractClientPlayerEntity> entries = MinecraftClient.getInstance().world.getPlayers();
                
                // 筛选出平民阵营且存活的玩家
                entries.removeIf((e) -> {
                    if (e.getUuid().equals(player.getUuid())) return true;
                    if (!GameFunctions.isPlayerAliveAndSurvival(e)) return true;
                    return !gameWorldComponent.getRole(e).isInnocent();
                });
                
                int apart = 36;
                int x = ((LimitedInventoryScreen)(Object)this).width / 2 - (entries.size()) * apart / 2 + 9;
                int shouldBeY = (((ExecutionerScreenMixin)(Object)this).height - 32) / 2;
                int y = shouldBeY + 80;

                for (int i = 0; i < entries.size(); ++i) {
                    ExecutionerPlayerWidget child = new ExecutionerPlayerWidget(x + apart * i, y, entries.get(i), i);
                    addDrawableChild(child);
                }
            }
        }
    }
}