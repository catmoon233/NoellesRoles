package org.agmas.noellesroles.client.mixin.swapper;

import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.PlayerPaginationHelper;
import org.agmas.noellesroles.client.RoleScreenHelper;
import org.agmas.noellesroles.client.SwapperPlayerWidget;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(LimitedInventoryScreen.class)
public abstract class SwapperScreenMixin extends LimitedHandledScreen<PlayerScreenHandler> implements PlayerPaginationHelper.ScreenWithChildren {
    @Unique
    private static final PlayerPaginationHelper.PaginationTextProvider TEXT_PROVIDER = new PlayerPaginationHelper.PaginationTextProvider() {
        @Override
        public String getPageTranslationKey() {
            return "hud.pagination.page";
        }

        @Override
        public String getPrevTranslationKey() {
            return "hud.pagination.prev";
        }

        @Override
        public String getNextTranslationKey() {
            return "hud.pagination.next";
        }
    };

    @Shadow @Final
    public ClientPlayerEntity player;

    @Unique
    private RoleScreenHelper<AbstractClientPlayerEntity> roleScreenHelper;

    public SwapperScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Overwrite
    protected void drawBackground(DrawContext drawContext, float v, int i, int i1) {

    }

    @Unique
    private RoleScreenHelper<AbstractClientPlayerEntity> getRoleScreenHelper() {
        if (roleScreenHelper == null) {
            roleScreenHelper = new RoleScreenHelper<>(
                player,
                Noellesroles.SWAPPER,
                this::createSwapperWidget,
                TEXT_PROVIDER,
                this::drawSwapperSelectionHint,
                this::getEligiblePlayers
            );
        }
        return roleScreenHelper;
    }

    @Unique
    private SwapperPlayerWidget createSwapperWidget(int x, int y, AbstractClientPlayerEntity playerEntity, int index) {
        SwapperPlayerWidget widget = new SwapperPlayerWidget(
            (LimitedInventoryScreen) (Object) this,
            x, y, playerEntity, index
        );
        addDrawableChild(widget);
        return widget;
    }

    @Unique
    private void drawSwapperSelectionHint(DrawContext context, java.awt.Point point) {
        MinecraftClient client = MinecraftClient.getInstance();
        Text text;
        int color;

        if (SwapperPlayerWidget.playerChoiceOne == null) {
            text = Text.translatable("hud.swapper.first_player_selection");
            color = Color.CYAN.getRGB();
        } else {
            text = Text.translatable("hud.swapper.second_player_selection");
            color = Color.RED.getRGB();
        }

        int textWidth = client.textRenderer.getWidth(text);
        context.drawTextWithShadow(client.textRenderer, text,
            point.x - textWidth / 2, point.y + 40, color);
    }

    @Unique
    private List<AbstractClientPlayerEntity> getEligiblePlayers() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return List.of();
        }

        return client.world.getPlayers().stream()
            .filter(p -> !p.getUuid().equals(player.getUuid()))
            .filter(this::isPlayerInAdventureMode)
            .collect(Collectors.toList());
    }

    @Unique
    private boolean isPlayerInAdventureMode(AbstractClientPlayerEntity targetPlayer) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }

        PlayerListEntry entry = client.player.networkHandler.getPlayerListEntry(targetPlayer.getUuid());
        return entry != null && entry.getGameMode() == GameMode.ADVENTURE;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void noellesroles$onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        getRoleScreenHelper().onRender(context, this);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void noellesroles$onInit(CallbackInfo ci) {
        SwapperPlayerWidget.playerChoiceOne = null;
        getRoleScreenHelper().onInit(this);
    }

    @Override
    public void addDrawableChild(ButtonWidget button) {
        super.addDrawableChild(button);
    }

    @Override
    public void removeDrawableChild(ButtonWidget button) {
        super.remove(button);
    }

    @Override
    public void clearChildren() {
        super.clearChildren();
    }
}