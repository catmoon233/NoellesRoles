package org.agmas.noellesroles.client.screen;

import dev.doctor4t.trainmurdermystery.api.Role;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.widget.ConspiratorPlayerWidget;
import org.agmas.noellesroles.client.widget.ConspiratorRoleWidget;
import org.agmas.noellesroles.packet.ConspiratorC2SPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 阴谋家选择屏幕
 * 
 * 两阶段选择：
 * 1. 选择目标玩家（显示所有玩家头像）
 * 2. 选择角色（显示所有可用角色）
 */
public class ConspiratorScreen extends Screen {
    
    // 当前阶段：0 = 选择玩家，1 = 选择角色
    private int phase = 0;
    
    // 选中的玩家
    private UUID selectedPlayer = null;
    private String selectedPlayerName = "";
    
    // 玩家列表
    private List<AbstractClientPlayer> players = new ArrayList<>();
    
    // 角色列表
    private List<Role> roles = new ArrayList<>();
    
    // Widget 列表
    private List<ConspiratorPlayerWidget> playerWidgets = new ArrayList<>();
    private List<ConspiratorRoleWidget> roleWidgets = new ArrayList<>();
    
    public ConspiratorScreen() {
        super(Component.translatable("screen.noellesroles.conspirator.title"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        // 清空旧的 widget
        playerWidgets.clear();
        roleWidgets.clear();
        
        if (phase == 0) {
            initPlayerSelection();
        } else {
            initRoleSelection();
        }
    }
    
    /**
     * 初始化玩家选择阶段
     */
    private void initPlayerSelection() {
        if (minecraft == null || minecraft.level == null || minecraft.player == null) return;
        
        // 获取所有其他玩家
        players = new ArrayList<>(minecraft.level.players());
        players.removeIf(p -> p.getUUID().equals(minecraft.player.getUUID()));
        
        if (players.isEmpty()) {
            onClose();
            return;
        }
        
        // 计算布局
        int columns = Math.min(players.size(), 8);
        int rows = (int) Math.ceil(players.size() / 8.0);
        int widgetSize = 32;
        int spacing = 8;
        int totalWidth = columns * (widgetSize + spacing) - spacing;
        int totalHeight = rows * (widgetSize + spacing) - spacing;
        int startX = (width - totalWidth) / 2;
        int startY = (height - totalHeight) / 2 + 20;
        
        for (int i = 0; i < players.size(); i++) {
            int col = i % 8;
            int row = i / 8;
            int x = startX + col * (widgetSize + spacing);
            int y = startY + row * (widgetSize + spacing);
            
            ConspiratorPlayerWidget widget = new ConspiratorPlayerWidget(
                this, x, y, widgetSize, players.get(i), i
            );
            playerWidgets.add(widget);
            addRenderableWidget(widget);
        }
    }
    
    /**
     * 初始化角色选择阶段
     */
    private void initRoleSelection() {
        // 获取所有注册的角色
        roles = Noellesroles.getEnableRoles();
        
        if (roles.isEmpty()) {
            onClose();
            return;
        }
        
        // 计算布局 - 角色名称需要更多空间
        int columns = Math.min(roles.size(), 5);
        int rows = (int) Math.ceil(roles.size() / 5.0);
        int widgetWidth = 120;
        int widgetHeight = 24;
        int spacingX = 10;
        int spacingY = 6;
        int totalWidth = columns * (widgetWidth + spacingX) - spacingX;
        int totalHeight = rows * (widgetHeight + spacingY) - spacingY;
        int startX = (width - totalWidth) / 2;
        int startY = (height - totalHeight) / 2 + 20;
        
        for (int i = 0; i < roles.size(); i++) {
            int col = i % 5;
            int row = i / 5;
            int x = startX + col * (widgetWidth + spacingX);
            int y = startY + row * (widgetHeight + spacingY);
            
            ConspiratorRoleWidget widget = new ConspiratorRoleWidget(
                this, x, y, widgetWidth, widgetHeight, roles.get(i), i
            );
            roleWidgets.add(widget);
            addRenderableWidget(widget);
        }
    }
    
    /**
     * 玩家被选中时调用
     */
    public void onPlayerSelected(UUID playerUuid, String playerName) {
        this.selectedPlayer = playerUuid;
        this.selectedPlayerName = playerName;
        this.phase = 1;
        
        // 重新初始化，显示角色选择
        clearWidgets();
        init();
    }
    
    /**
     * 角色被选中时调用
     */
    public void onRoleSelected(Role role) {
        if (selectedPlayer == null) return;
        if (minecraft == null || minecraft.player == null) return;
        
        // 发送网络包到服务端
        ClientPlayNetworking.send(new ConspiratorC2SPacket(
            selectedPlayer,
            role.identifier().toString()
        ));
        
        // 消耗书页物品
        ItemStack mainHand = minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = minecraft.player.getItemInHand(InteractionHand.OFF_HAND);
        
        // 物品消耗由服务端处理
        
        // 关闭屏幕
        onClose();
    }
    
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // 渲染背景
        renderBackground(context, mouseX, mouseY, delta);
        
        // 渲染标题
        Component title;
        if (phase == 0) {
            title = Component.translatable("screen.noellesroles.conspirator.select_player")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        } else {
            title = Component.translatable("screen.noellesroles.conspirator.select_role", selectedPlayerName)
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        }
        
        context.drawCenteredString(font, title, width / 2, 30, 0xFFFFFF);
        
        // 渲染提示
        Component hint = Component.translatable("screen.noellesroles.conspirator.hint")
            .withStyle(ChatFormatting.GRAY);
        context.drawCenteredString(font, hint, width / 2, height - 30, 0x888888);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC 键返回上一阶段或关闭
        if (keyCode == 256) { // ESC
            if (phase == 1) {
                // 返回玩家选择阶段
                phase = 0;
                selectedPlayer = null;
                selectedPlayerName = "";
                clearWidgets();
                init();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}