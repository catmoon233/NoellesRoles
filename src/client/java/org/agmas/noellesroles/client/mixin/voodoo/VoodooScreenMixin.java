package org.agmas.noellesroles.client.mixin.voodoo;

import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.agmas.noellesroles.ConfigWorldComponent;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.PlayerPaginationHelper;
import org.agmas.noellesroles.client.RoleScreenHelper;
import org.agmas.noellesroles.client.VoodooPlayerWidget;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mixin(LimitedInventoryScreen.class)
public abstract class VoodooScreenMixin extends LimitedHandledScreen<PlayerScreenHandler> implements PlayerPaginationHelper.ScreenWithChildren {
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
    private RoleScreenHelper<UUID> roleScreenHelper;

    public VoodooScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }


    @Unique
    private RoleScreenHelper<UUID> getRoleScreenHelper() {
        if (roleScreenHelper == null) {
            MinecraftClient client = MinecraftClient.getInstance();
            roleScreenHelper = new RoleScreenHelper<>(
                player,
                Noellesroles.VOODOO,
                this::createVoodooWidget,
                TEXT_PROVIDER,
                this::drawVoodooTip,
                this::getEligiblePlayers
            );
        }
        return roleScreenHelper;
    }

    @Unique
    private VoodooPlayerWidget createVoodooWidget(int x, int y, UUID playerUUID, int index) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return null;
        }

        PlayerListEntry playerListEntry = client.player.networkHandler.getPlayerListEntry(playerUUID);
        if (playerListEntry == null) {
            return null;
        }

        VoodooPlayerWidget widget = new VoodooPlayerWidget(
            (LimitedInventoryScreen) (Object) this,
            x, y, playerUUID, playerListEntry, index
        );
        addDrawableChild(widget);
        return widget;
    }

    @Unique
    private void drawVoodooTip(DrawContext context, java.awt.Point point) {
        ConfigWorldComponent configComponent = ConfigWorldComponent.KEY.get(player.getWorld());
        if (!configComponent.naturalVoodoosAllowed) {
            MinecraftClient client = MinecraftClient.getInstance();
            Text text = Text.translatable("hud.voodoo.tip");
            int textWidth = client.textRenderer.getWidth(text);
            context.drawTextWithShadow(client.textRenderer, text,
                point.x - textWidth / 2, point.y + 40, Color.RED.getRGB());
        }
    }

    @Unique
    private List<UUID> getEligiblePlayers() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return List.of();
        }

        return client.player.networkHandler.getPlayerUuids().stream()
                .filter(uuid -> !uuid.equals(player.getUuid()))
                .filter(uuid -> {
                    PlayerListEntry entry = client.player.networkHandler.getPlayerListEntry(uuid);
                    return entry != null && entry.getGameMode() == GameMode.ADVENTURE;
                })
                .collect(Collectors.toList());
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void noellesroles$onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        getRoleScreenHelper().onRender(context, this);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void noellesroles$onInit(CallbackInfo ci) {
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