package org.agmas.noellesroles.client.screen;

import dev.doctor4t.trainmurdermystery.api.Role;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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
    
    // 翻页相关
    private static final int ROLES_PER_PAGE = 12; // 每页最多12个角色
    private int currentRolePage = 0; // 当前角色页码
    private Button prevPageButton;
    private Button nextPageButton;
    
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
        
        // 计算总页数
        int totalPages = getTotalRolePages();
        
        // 确保当前页码有效
        if (currentRolePage >= totalPages) {
            currentRolePage = totalPages - 1;
        }
        if (currentRolePage < 0) {
            currentRolePage = 0;
        }
        
        // 计算当前页的角色范围
        int startIndex = currentRolePage * ROLES_PER_PAGE;
        int endIndex = Math.min(startIndex + ROLES_PER_PAGE, roles.size());
        int rolesOnThisPage = endIndex - startIndex;
        
        // 计算布局 - 每页最多12个角色，4列3行
        int columns = Math.min(rolesOnThisPage, 4);
        int rows = (int) Math.ceil(rolesOnThisPage / 4.0);
        int widgetWidth = 120;
        int widgetHeight = 24;
        int spacingX = 10;
        int spacingY = 6;
        int totalWidth = columns * (widgetWidth + spacingX) - spacingX;
        int totalHeight = rows * (widgetHeight + spacingY) - spacingY;
        int startX = (width - totalWidth) / 2;
        int startY = (height - totalHeight) / 2 + 10;
        
        // 添加当前页的角色
        for (int i = startIndex; i < endIndex; i++) {
            int indexOnPage = i - startIndex;
            int col = indexOnPage % 4;
            int row = indexOnPage / 4;
            int x = startX + col * (widgetWidth + spacingX);
            int y = startY + row * (widgetHeight + spacingY);
            
            ConspiratorRoleWidget widget = new ConspiratorRoleWidget(
                this, x, y, widgetWidth, widgetHeight, roles.get(i), i
            );
            roleWidgets.add(widget);
            addRenderableWidget(widget);
        }
        
        // 添加翻页按钮
        int buttonWidth = 60;
        int buttonHeight = 20;
        int buttonY = startY + totalHeight + 20;
        
        // 上一页按钮
        prevPageButton = Button.builder(
            Component.translatable("screen.noellesroles.conspirator.prev_page"),
            button -> {
                if (currentRolePage > 0) {
                    currentRolePage--;
                    refreshRoleSelection();
                }
            }
        ).bounds(width / 2 - buttonWidth - 30, buttonY, buttonWidth, buttonHeight).build();
        prevPageButton.active = currentRolePage > 0;
        addRenderableWidget(prevPageButton);
        
        // 下一页按钮
        nextPageButton = Button.builder(
            Component.translatable("screen.noellesroles.conspirator.next_page"),
            button -> {
                if (currentRolePage < totalPages - 1) {
                    currentRolePage++;
                    refreshRoleSelection();
                }
            }
        ).bounds(width / 2 + 30, buttonY, buttonWidth, buttonHeight).build();
        nextPageButton.active = currentRolePage < totalPages - 1;
        addRenderableWidget(nextPageButton);
    }
    
    /**
     * 获取角色总页数
     */
    private int getTotalRolePages() {
        return (int) Math.ceil(roles.size() / (double) ROLES_PER_PAGE);
    }
    
    /**
     * 刷新角色选择界面
     */
    private void refreshRoleSelection() {
        clearWidgets();
        roleWidgets.clear();
        initRoleSelection();
    }
    
    /**
     * 玩家被选中时调用
     */
    public void onPlayerSelected(UUID playerUuid, String playerName) {
        this.selectedPlayer = playerUuid;
        this.selectedPlayerName = playerName;
        this.phase = 1;
        this.currentRolePage = 0; // 重置页码
        
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
        
        // 渲染页码信息（仅在角色选择阶段）
        if (phase == 1 && roles.size() > ROLES_PER_PAGE) {
            int totalPages = getTotalRolePages();
            Component pageInfo = Component.translatable("screen.noellesroles.conspirator.page_info",
                currentRolePage + 1, totalPages)
                .withStyle(ChatFormatting.YELLOW);
            context.drawCenteredString(font, pageInfo, width / 2, 45, 0xFFFFFF);
        }
        
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
                currentRolePage = 0; // 重置页码
                clearWidgets();
                init();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}