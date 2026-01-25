package org.agmas.noellesroles.client.screen;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.utils.RoleUtils;
import org.agmas.noellesroles.client.widget.GamblerRoleWidget;
import org.agmas.noellesroles.packet.GamblerSelectRoleC2SPacket;
import org.agmas.noellesroles.roles.gambler.GamblerPlayerComponent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class GamblerScreen extends Screen {
    private final GamblerPlayerComponent component;
    private final List<Role> availableRoles = new ArrayList<>();
    private Role selectedRole;

    // 搜索框
    EditBox searchWidget = null;
    String searchContent = null;
    int totalPages = 0;

    private List<GamblerRoleWidget> roleWidgets = new ArrayList<>();

    // 翻页相关
    private static final int ROLES_PER_PAGE = 12; // 每页最多12个角色
    private int currentRolePage = 0; // 当前角色页码
    private Button prevPageButton;
    private Button nextPageButton;

    /**
     * 选择阶段：搜索职业
     */
    private void onRoleSearch(String text) {
        if (text == "") {
            searchContent = null;
        } else {
            searchContent = text;
        }
        currentRolePage = 0;
        totalPages = 0;
        refreshRoleSelection();
    }

    public GamblerScreen(Player player) {
        super(Component.translatable("gui.noellesroles.gambler.title"));
        this.component = GamblerPlayerComponent.KEY.get(player);

        // 加载可用角色
        for (ResourceLocation roleId : component.availableRoles) {
            for (Role role : Noellesroles.getEnableRoles()) {
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

    /**
     * 初始化角色选择阶段
     */
    private void initRoleSelection() {
        // 获取所有注册的角色

        if (availableRoles.isEmpty()) {
            onClose();
            return;
        }

        if (totalPages > 0) {
            // 确保当前页码有效
            if (currentRolePage >= totalPages) {
                currentRolePage = totalPages - 1;
            }
            if (currentRolePage < 0) {
                currentRolePage = 0;
            }
        }

        // 计算当前页的角色范围
        int startIndex = currentRolePage * ROLES_PER_PAGE;
        int endIndex = Math.min(startIndex + ROLES_PER_PAGE, availableRoles.size());
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
        int count = 0;

        for (int i = 0; i < availableRoles.size(); i++) {
            var role = availableRoles.get(i);
            String roleName = RoleUtils.getRoleName(role).getString();
            if (searchContent == null || roleName.contains(searchContent)) {
                if (count >= startIndex && count < endIndex) {
                    int indexOnPage = count - startIndex;
                    int col = indexOnPage % 4;
                    int row = indexOnPage / 4;
                    int x = startX + col * (widgetWidth + spacingX);
                    int y = startY + row * (widgetHeight + spacingY);

                    GamblerRoleWidget widget = new GamblerRoleWidget(
                            this, x, y, widgetWidth, widgetHeight, availableRoles.get(i), i);
                    roleWidgets.add(widget);
                    addRenderableWidget(widget);

                }
                count++;
            }
        }

        totalPages = (int) Math.ceil(count / (double) ROLES_PER_PAGE);
        // 添加翻页按钮
        int buttonWidth = 60;
        int buttonHeight = 20;
        int buttonY = startY + totalHeight + 20;
        // 上一页按钮
        if (prevPageButton != null) {

        }
        prevPageButton = Button.builder(
                Component.translatable("screen.noellesroles.conspirator.prev_page"),
                button -> {
                    if (currentRolePage > 0) {
                        currentRolePage--;
                        refreshRoleSelection();
                    }
                }).bounds(width / 2 - buttonWidth - 30, buttonY, buttonWidth, buttonHeight).build();
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
                }).bounds(width / 2 + 30, buttonY, buttonWidth, buttonHeight).build();
        nextPageButton.active = currentRolePage < totalPages - 1;
        addRenderableWidget(nextPageButton);
        // 避免重复创建组件
        if (searchWidget == null) {
            searchWidget = new EditBox(font, startX, startY - 40, totalWidth, 20, Component.nullToEmpty(""));
            searchWidget.setEditable(true);
            searchWidget.setResponder((text) -> {
                onRoleSearch(text);
            });
            addRenderableWidget(searchWidget);
        }
        if (count <= 0) {
            // 没有
            searchWidget.setTextColor(Color.RED.getRGB());
        } else {
            searchWidget.setTextColor(Color.WHITE.getRGB());
        }

    }

    /**
     * 刷新角色选择界面
     */
    private void refreshRoleSelection() {
        for (int i = 0; i < roleWidgets.size(); i++) {
            this.removeWidget(roleWidgets.get(i));
        }
        this.removeWidget(prevPageButton);
        this.removeWidget(nextPageButton);
        roleWidgets.clear();
        initRoleSelection();
    }

    /**
     * 角色被选中时调用
     */
    public void onRoleSelected(Role role) {
        if (minecraft == null || minecraft.player == null)
            return;
        this.selectedRole = role;
        if (this.selectedRole == null) {
            return;
        }
        ClientPlayNetworking.send(new GamblerSelectRoleC2SPacket(this.selectedRole.identifier()));
        // 关闭屏幕
        onClose();
    }

    @Override
    protected void init() {
        super.init();
        initRoleSelection();
        return;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int startY = (this.height - 166) / 2;

        guiGraphics.drawCenteredString(font, this.title, width / 2, 30, 0xFFFFFF);

        if (selectedRole != null) {
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("gui.noellesroles.gambler.selected", RoleUtils.getRoleName(selectedRole)),
                    this.width / 2, startY + 200, 0x00FF00);
        }
        Component pageInfo = Component.translatable("screen.noellesroles.gambler.page_info",
                currentRolePage + 1, totalPages)
                .withStyle(ChatFormatting.YELLOW);
        guiGraphics.drawCenteredString(font, pageInfo, width / 2, 45, 0xFFFFFF);
        Component hint = Component.translatable("screen.noellesroles.gambler.hint")
                .withStyle(ChatFormatting.GRAY);
        guiGraphics.drawCenteredString(font, hint, width / 2, height - 30, 0x888888);
    }
}