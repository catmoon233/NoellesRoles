package org.agmas.noellesroles.client.screen;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.packet.GamblerSelectRoleC2SPacket;
import org.agmas.noellesroles.roles.gambler.GamblerPlayerComponent;

import java.util.ArrayList;
import java.util.List;

public class GamblerScreen extends Screen {
    private final GamblerPlayerComponent component;
    private final List<Role> availableRoles = new ArrayList<>();
    private Role selectedRole;
    private int page = 0;
    private static final int ROLES_PER_PAGE = 12;

    public GamblerScreen(Player player) {
        super(Component.translatable("gui.noellesroles.gambler.title"));
        this.component = GamblerPlayerComponent.KEY.get(player);
        
        // 加载可用角色
        for (ResourceLocation roleId : component.availableRoles) {
            for (Role role : TMMRoles.ROLES) {
                if (role.identifier().equals(roleId)) {
                    availableRoles.add(role);
                    break;
                }
            }
        }
        
        if (component.selectedRole != null) {
            for (Role role : availableRoles) {
                if (role.identifier().equals(component.selectedRole)) {
                    selectedRole = role;
                    break;
                }
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        
        int startX = (this.width - 176) / 2;
        int startY = (this.height - 166) / 2;
        
        // 添加角色按钮
        int rolesToShow = Math.min(availableRoles.size() - page * ROLES_PER_PAGE, ROLES_PER_PAGE);
        for (int i = 0; i < rolesToShow; i++) {
            int index = page * ROLES_PER_PAGE + i;
            Role role = availableRoles.get(index);
            
            int x = startX + 10 + (i % 4) * 40;
            int y = startY + 20 + (i / 4) * 40;
            
            this.addRenderableWidget(Button.builder(Component.literal(role.identifier().getPath()), button -> {
                this.selectedRole = role;
                ClientPlayNetworking.send(new GamblerSelectRoleC2SPacket(role.identifier()));
            })
            .bounds(x, y, 38, 38)
            .tooltip(null) // 可以添加角色描述
            .build());
        }
        
        // 翻页按钮
        if (availableRoles.size() > ROLES_PER_PAGE) {
            this.addRenderableWidget(Button.builder(Component.literal("<"), button -> {
                if (page > 0) {
                    page--;
                    this.rebuildWidgets();
                }
            }).bounds(startX + 10, startY + 140, 20, 20).build());
            
            this.addRenderableWidget(Button.builder(Component.literal(">"), button -> {
                if ((page + 1) * ROLES_PER_PAGE < availableRoles.size()) {
                    page++;
                    this.rebuildWidgets();
                }
            }).bounds(startX + 146, startY + 140, 20, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        int startX = (this.width - 176) / 2;
        int startY = (this.height - 166) / 2;
        
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, startY + 6, 0xFFFFFF);
        
        if (selectedRole != null) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.noellesroles.gambler.selected", selectedRole.identifier().getPath()), this.width / 2, startY + 150, 0x00FF00);
        }
    }
}