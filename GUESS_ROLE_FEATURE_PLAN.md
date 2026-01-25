# 身份猜测记录功能实施计划

## 目标
添加一个客户端功能，允许玩家通过 GUI 记录他们对其他玩家身份的猜测。

## 功能需求
1.  **按键触发**：按下指定按键（默认 'N'）打开 GUI。
2.  **玩家列表**：显示所有玩家的头像和名字。
3.  **猜测记录**：在玩家头像下显示猜测的职业（默认为 "???"）。
4.  **职业选择**：点击玩家头像进入职业选择界面，选择后更新记录。
5.  **分页**：玩家列表每页最多显示 9 人。
6.  **数据清理**：游戏结束（断开连接）时自动清除记录。

## 技术实现

### 1. 数据结构
在 `GuessRoleScreen` 类中维护一个静态 Map：
```java
public static Map<UUID, String> guessedRoles = new HashMap<>();
```

### 2. GUI 类 (`GuessRoleScreen`)
*   **位置**: `src/main/java/org/agmas/noellesroles/client/screen/GuessRoleScreen.java`
*   **继承**: `Screen`
*   **逻辑**:
    *   **Phase 0 (玩家选择)**:
        *   获取当前世界所有玩家。
        *   过滤掉自己。
        *   分页显示（每页 9 个，3x3 网格）。
        *   使用 `GuessPlayerWidget` 渲染。
    *   **Phase 1 (职业选择)**:
        *   获取所有可用职业 (`Noellesroles.getEnableRoles()`)。
        *   分页显示职业列表。
        *   点击职业后更新 `guessedRoles` 并返回 Phase 0。

### 3. Widget 类 (`GuessPlayerWidget`)
*   **位置**: `src/main/java/org/agmas/noellesroles/client/widget/GuessPlayerWidget.java`
*   **继承**: `Button`
*   **渲染**:
    *   背景框。
    *   玩家头像 (`PlayerFaceRenderer`)。
    *   猜测的职业名称（居中显示在头像下方）。
    *   悬停时显示玩家名字 Tooltip。

### 4. 客户端注册 (`NoellesrolesClient`)
*   **按键注册**:
    ```java
    public static KeyMapping guessRoleBind;
    // 在 onInitializeClient 中注册
    guessRoleBind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        "key.noellesroles.guess_role",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_N,
        "category.trainmurdermystery.keybinds"
    ));
    ```
*   **事件监听**:
    *   `ClientTickEvents.END_CLIENT_TICK`: 检查按键，打开 GUI。
    *   `ClientPlayConnectionEvents.DISCONNECT`: 调用 `GuessRoleScreen.clearData()`。

### 5. 语言文件
*   添加必要的翻译键到 `zh_cn.json`。

## 实施步骤
1.  创建 `GuessPlayerWidget.java`。
2.  创建 `GuessRoleScreen.java`。
3.  修改 `NoellesrolesClient.java` 注册按键和事件。
4.  更新语言文件。