package org.agmas.noellesroles.roles.thief;

import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.ChatFormatting;
import org.agmas.noellesroles.utils.RoleUtils;
import java.util.OptionalInt;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.init.ModItems;
import org.agmas.noellesroles.repack.HSRItems;
import org.agmas.noellesroles.role.ModRoles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

/**
 * 小偷玩家组件
 *
 * 中立阵营，假心情，无限冲刺时间
 *
 * 技能：
 * - 蹲下按技能键切换偷钱/偷物品模式
 * - 按技能键释放技能（冷却30s，偷取失败不进入冷却）
 * - 偷钱：偷取目标100金币（目标必须至少有100金币）
 * - 偷物品：仿照StupidExpress2的小偷机制
 *
 * 被动：
 * - 杀一人获得100金币
 *
 * 独立胜利条件：
 * - 场上存在小偷时游戏不结束
 * - 手持小偷的荣誉（金锭）回房间睡觉则独立胜利
 * - 小偷的荣誉所需金币数 = 游戏开始总人数 * 75
 */
public class ThiefPlayerComponent implements RoleComponent, ServerTickingComponent {

    public static int honorCost = 0;
    /** 组件键 */
    public static final ComponentKey<ThiefPlayerComponent> KEY = ComponentRegistry.getOrCreate(
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "thief"),
            ThiefPlayerComponent.class);

    /** 技能冷却时间（30秒 = 600 tick） */
    public static final int ABILITY_COOLDOWN = 30 * 20;

    /** 每次偷钱金额 */
    public static final int STEAL_MONEY_AMOUNT = 100;

    /** 购买小偷的荣誉所需的金币基数 */
    public static final int HONOR_COST_PER_PLAYER = 75;

    /** 偷钱模式 */
    public static final int MODE_STEAL_MONEY = 0;

    /** 偷物品模式 */
    public static final int MODE_STEAL_ITEM = 1;

    private final Player player;

    /** 技能冷却 */
    public int cooldown = 0;

    /** 当前模式：0=偷钱, 1=偷物品 */
    public int currentMode = MODE_STEAL_MONEY;

    /** 是否在偷取选择界面（蹲下状态） */
    public boolean isInSelectionMode = false;

    /**
     * 构造函数
     */
    public ThiefPlayerComponent(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void reset() {
        this.cooldown = 0;
        this.currentMode = MODE_STEAL_MONEY;
        this.isInSelectionMode = false;
        this.sync();
    }

    @Override
    public void clear() {
        this.reset();
    }

    /**
     * 切换偷取模式（蹲下按技能键）
     */
    public void toggleMode() {
        if (this.cooldown > 0) {
            return;
        }

        if (this.currentMode == MODE_STEAL_MONEY) {
            this.currentMode = MODE_STEAL_ITEM;
        } else {
            this.currentMode = MODE_STEAL_MONEY;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            Component message;
            if (this.currentMode == MODE_STEAL_MONEY) {
                message = Component.translatable("message.noellesroles.thief.mode.money")
                        .withStyle(ChatFormatting.GOLD);
            } else {
                message = Component.translatable("message.noellesroles.thief.mode.item")
                        .withStyle(ChatFormatting.AQUA);
            }
            serverPlayer.displayClientMessage(message, true);
        }

        this.sync();
    }

    /**
     * 尝试使用技能（按技能键释放）
     * 
     * @return 是否成功释放技能
     */
    public boolean useAbility() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        // 检查冷却
        if (this.cooldown > 0) {
            return false;
        }

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());

        // 检查角色
        if (!gameWorld.isRole(player, ModRoles.THIEF)) {
            return false;
        }

        // 获取当前看向的目标玩家
        Player target = getLookedAtPlayer();
        if (target == null) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.thief.no_target")
                            .withStyle(ChatFormatting.RED),
                    true);
            return true; // 失败不进入冷却
        }

        if (this.currentMode == MODE_STEAL_MONEY) {
            return stealMoney(target);
        } else {
            return stealItem(target);
        }
    }

    /**
     * 偷钱
     */
    private boolean stealMoney(Player target) {
        if (!(player instanceof ServerPlayer serverPlayer) || !(target instanceof ServerPlayer targetPlayer)) {
            return false;
        }

        // 检查目标是否被淘汰
        if (GameFunctions.isPlayerEliminated(target)) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.thief.target_eliminated")
                            .withStyle(ChatFormatting.RED),
                    true);
            return true; // 失败不进入冷却
        }

        // 获取目标金钱
        PlayerShopComponent targetShop = PlayerShopComponent.KEY.get(target);
        int targetBalance = targetShop.balance;

        // 检查目标金币是否足够
        if (targetBalance < STEAL_MONEY_AMOUNT) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.thief.not_enough_money",
                            target.getDisplayName())
                            .withStyle(ChatFormatting.RED),
                    true);
            return true; // 失败不进入冷却
        }

        // 偷取金币
        targetShop.balance -= STEAL_MONEY_AMOUNT;
        targetShop.sync();

        PlayerShopComponent thiefShop = PlayerShopComponent.KEY.get(player);
        thiefShop.balance += STEAL_MONEY_AMOUNT;
        thiefShop.sync();

        // 通知小偷
        serverPlayer.displayClientMessage(
                Component.translatable("message.noellesroles.thief.stole_money",
                        target.getDisplayName(),
                        STEAL_MONEY_AMOUNT)
                        .withStyle(ChatFormatting.GOLD),
                true);

        // 通知被偷者
        targetPlayer.displayClientMessage(
                Component.translatable("message.noellesroles.thief.money_stolen",
                        STEAL_MONEY_AMOUNT)
                        .withStyle(ChatFormatting.RED),
                true);

        // 成功偷取，进入冷却
        this.cooldown = ABILITY_COOLDOWN;
        this.sync();

        return true;
    }

    /**
     * 偷物品（仿照StupidExpress2的小偷）
     */
    private boolean stealItem(Player target) {
        if (!(player instanceof ServerPlayer serverPlayer) || !(target instanceof ServerPlayer targetPlayer)) {
            return false;
        }

        // 检查目标是否被淘汰
        if (GameFunctions.isPlayerEliminated(target)) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.thief.target_eliminated")
                            .withStyle(ChatFormatting.RED),
                    true);
            return true;
        }

        // 统计可偷取物品数量
        int count = 0;
        for (ItemStack stack : target.getInventory().items) {
            if (!stack.isEmpty() && canStealItem(stack)) {
                count++;
            }
        }

        // 如果没有可偷物品
        if (count == 0) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.thief.no_stealable_items",
                            target.getDisplayName())
                            .withStyle(ChatFormatting.YELLOW),
                    true);
            return true; // 没有物品可偷，不进入冷却
        }

        // 随机选择一个物品
        int targetIndex = player.getRandom().nextInt(count);
        int currentIndex = 0;
        int slotIndex = -1;
        ItemStack stolenItem = ItemStack.EMPTY;

        for (int i = 0; i < target.getInventory().items.size(); i++) {
            ItemStack stack = target.getInventory().items.get(i);
            if (!stack.isEmpty() && canStealItem(stack)) {
                if (currentIndex == targetIndex) {
                    slotIndex = i;
                    stolenItem = stack.copy();
                    break;
                }
                currentIndex++;
            }
        }

        if (slotIndex == -1 || stolenItem.isEmpty()) {
            return true;
        }

        // 先获取物品名称（在移除之前）
        Component itemName = stolenItem.getDisplayName();

        // 从目标物品栏移除物品
        target.getInventory().items.set(slotIndex, ItemStack.EMPTY);

        // 检查小偷背包是否有空间
        boolean canAdd = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (slot.isEmpty()) {
                canAdd = true;
                break;
            }
        }

        if (!canAdd) {
            // 背包满了，归还物品给目标
            target.getInventory().items.set(slotIndex, stolenItem);
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.thief.inventory_full")
                            .withStyle(ChatFormatting.RED),
                    true);
            return true; // 失败不进入冷却
        }

        // 给小偷物品
        player.getInventory().add(stolenItem);

        // 通知小偷
        serverPlayer.displayClientMessage(
                Component.translatable("message.noellesroles.thief.stole_item",
                        target.getDisplayName(),
                        itemName)
                        .withStyle(ChatFormatting.AQUA),
                true);

        // 通知被偷者
        targetPlayer.displayClientMessage(
                Component.translatable("message.noellesroles.thief.item_stolen",
                        itemName)
                        .withStyle(ChatFormatting.RED),
                true);

        // 成功偷取，进入冷却
        this.cooldown = ABILITY_COOLDOWN;
        this.sync();

        return true;
    }

    /**
     * 判断物品是否可以被偷取
     * 只允许偷取指定的武器和道具
     */
    private boolean canStealItem(ItemStack stack) {
        if (stack.isEmpty())
            return false;

        // 禁止偷取的物品
        // 金锭（小偷的荣誉）
        if (stack.is(Items.GOLD_INGOT))
            return false;

        // 只允许偷取以下物品：

        // 枪械类
        if (stack.is(TMMItems.REVOLVER))
            return true; // 左轮手枪
        if (stack.is(HSRItems.BANDIT_REVOLVER))
            return true; // 匪徒手枪
        if (stack.is(ModItems.PATROLLER_REVOLVER))
            return true; // 巡警手枪
        if (stack.is(TMMItems.DERRINGER))
            return true; // 德林加手枪
        if (stack.is(ModItems.ONCE_REVOLVER))
            return true; // 一次性手枪

        // 武器类
        if (stack.is(TMMItems.KNIFE))
            return true; // 匕首
        if (stack.is(TMMItems.BAT))
            return true; // 球棒（小巧思）

        // 投掷物类
        if (stack.is(TMMItems.GRENADE))
            return true; // 手榴弹
        if (stack.is(TMMItems.FIRECRACKER))
            return true; // 鞭炮
        if (stack.is(ModItems.BOMB))
            return true; // 炸弹

        // 道具类
        if (stack.is(TMMItems.SCORPION))
            return true; // 蝎子
        if (stack.is(TMMItems.POISON_VIAL))
            return true; // 毒药瓶
        if (stack.is(TMMItems.CROWBAR))
            return true; // 撬棍
        if (stack.is(TMMItems.LOCKPICK))
            return true; // 开锁器
        if (stack.is(TMMItems.BODY_BAG))
            return true; // 裹尸袋
        if (stack.is(TMMItems.NOTE))
            return true; // 纸条
        if (stack.is(ModItems.HANDCUFFS))
            return true; // 手铐

        // 特殊物品类（来自HSRItems）
        if (stack.is(HSRItems.TOXIN))
            return true; // 毒针
        if (stack.is(HSRItems.ANTIDOTE))
            return true; // 解药

        // NoellesRoles 特殊物品
        if (stack.is(ModItems.BOXING_GLOVE))
            return true; // 拳套
        if (stack.is(ModItems.DEFIBRILLATOR))
            return true; // 除颤仪
        if (stack.is(ModItems.DELUSION_VIAL))
            return true; // 幻觉试剂
        if (stack.is(ModItems.ANTIDOTE_REAGENT))
            return true; // 解药试剂
        if (stack.is(ModItems.BLANK_CARTRIDGE))
            return true; // 空包弹
        if (stack.is(ModItems.SMOKE_GRENADE))
            return true; // 烟雾弹
        if (stack.is(ModItems.REINFORCEMENT))
            return true; // 加固门道具
        if (stack.is(ModItems.ALARM_TRAP))
            return true; // 警报陷阱
        if (stack.is(ModItems.LOCK_ITEM))
            return true; // 锁
        if (stack.is(ModItems.DELIVERY_BOX))
            return true; // 传递盒
        if (stack.is(ModItems.HALLUCINATION_BOTTLE))
            return true; // 迷幻瓶
        if (stack.is(ModItems.NIGHT_VISION_GLASSES))
            return true; // 夜视镜
        if (stack.is(ModItems.WHEELCHAIR))
            return true; // 轮椅

        // 护盾试剂（来自TMM）
        if (stack.is(TMMItems.DEFENSE_VIAL))
            return true; // 护盾试剂

        // 万能钥匙和乘务员钥匙
        if (stack.is(ModItems.MASTER_KEY))
            return true;
        if (stack.is(ModItems.MASTER_KEY_P))
            return true;

        // 其他物品不可偷取
        return false;
    }

    /**
     * 获取当前看向的玩家
     */
    private Player getLookedAtPlayer() {
        double maxDistance = 4.0;
        Player closestPlayer = null;
        double closestDistance = maxDistance;

        for (Player otherPlayer : player.level().players()) {
            if (otherPlayer == player)
                continue;
            if (GameFunctions.isPlayerEliminated(otherPlayer))
                continue;

            double distance = player.distanceTo(otherPlayer);
            if (distance < closestDistance && player.hasLineOfSight(otherPlayer)) {
                closestDistance = distance;
                closestPlayer = otherPlayer;
            }
        }

        return closestPlayer;
    }

    /**
     * 处理小偷击杀目标（获得100金币）
     */
    public void handleKilledVictim(Player victim) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (!gameWorld.isRole(player, ModRoles.THIEF)) {
            return;
        }

        // 给予100金币
        PlayerShopComponent thiefShop = PlayerShopComponent.KEY.get(player);
        thiefShop.balance += 100;
        thiefShop.sync();

        // 通知小偷
        serverPlayer.displayClientMessage(
                Component.translatable("message.noellesroles.thief.kill_reward", 100)
                        .withStyle(ChatFormatting.GOLD),
                true);
    }

    /**
     * 检查小偷独立胜利条件
     * 手持小偷的荣誉（金锭）回房间睡觉则胜利
     */
    public static boolean checkThiefVictory(ServerLevel serverLevel) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(serverLevel);

        // 检查是否有小偷存活
        boolean hasThiefAlive = false;
        ServerPlayer thief = null;

        for (ServerPlayer p : serverLevel.players()) {
            if (gameWorld.isRole(p, ModRoles.THIEF)
                    && !GameFunctions.isPlayerEliminated(p)) {
                hasThiefAlive = true;
                thief = p;
                break;
            }
        }

        if (!hasThiefAlive || thief == null) {
            return false;
        }

        // 检查小偷是否手持小偷的荣誉（金锭）
        ItemStack heldItem = thief.getMainHandItem();
        if (!heldItem.is(Items.GOLD_INGOT)) {
            return false;
        }

        // 检查小偷是否在睡觉
        if (!thief.isSleeping()) {
            return false;
        }

        // 小偷胜利！
        RoleUtils.customWinnerWin(serverLevel, GameFunctions.WinStatus.CUSTOM, "thief",
                OptionalInt.of(new java.awt.Color(255, 215, 0).getRGB()));

        return true;
    }

    /**
     * 获取购买小偷的荣誉所需金币数
     */
    public static int getHonorCost(int totalPlayers) {
        return totalPlayers * HONOR_COST_PER_PLAYER;
    }

    @Override
    public void serverTick() {
        // 减少冷却
        if (this.cooldown > 0) {
            this.cooldown--;
            if (this.cooldown % 20 == 0 || this.cooldown == 0) {
                this.sync();
            }
        }
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registryLookup) {
        tag.putInt("Cooldown", this.cooldown);
        tag.putInt("CurrentMode", this.currentMode);
        tag.putBoolean("IsInSelectionMode", this.isInSelectionMode);
        var gameC = GameWorldComponent.KEY.get(this.player);
        if (gameC.isRole(this.player, ModRoles.THIEF)) {
            tag.putInt("honorCost", honorCost);
        }
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registryLookup) {
        honorCost = tag.contains("honorCost") ? tag.getInt("honorCost") : -1;
        this.cooldown = tag.getInt("Cooldown");
        this.currentMode = tag.getInt("CurrentMode");
        this.isInSelectionMode = tag.getBoolean("IsInSelectionMode");
    }
}
