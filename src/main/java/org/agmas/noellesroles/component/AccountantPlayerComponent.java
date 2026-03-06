package org.agmas.noellesroles.component;

import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

/**
 * 会计玩家组件
 *
 * 平民阵营，真实心情，默认冲刺时间
 *
 * 被动：每60秒获得25金币
 *
 * 技能：
 * - 蹲下按技能键：切换模式（收入模式 / 支出模式）
 * - 直接按技能键：花费175金币发动技能
 *
 * 收入模式：对玩家按下技能键查看其金币量是否超过300，如果是则给会计消息提示
 * 支出模式：查看半径4格内玩家30秒内总支出金币数量的大致范围（整百区间）
 *
 * 商店：可花费100金币购买存折
 */
public class AccountantPlayerComponent implements RoleComponent, ServerTickingComponent {

    /** 组件键 */
    public static final ComponentKey<AccountantPlayerComponent> KEY = ComponentRegistry.getOrCreate(
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "accountant"),
            AccountantPlayerComponent.class);

    /** 被动收入金币数 */
    public static final int PASSIVE_INCOME_AMOUNT = 25;

    /** 被动收入间隔（60秒 = 1200 tick） */
    public static final int PASSIVE_INCOME_INTERVAL = 60 * 20;

    /** 技能花费金币数 */
    public static final int SKILL_COST = 175;

    /** 收入金币查询阈值 */
    public static final int INCOME_QUERY_THRESHOLD = 300;

    /** 支出查询半径（4格） */
    public static final double EXPENSE_QUERY_RADIUS = 4.0;

    /** 支出查询时间范围（30秒 = 600 tick） */
    public static final int EXPENSE_QUERY_TIME_RANGE = 30 * 20;

    /** 收入模式 */
    public static final int MODE_INCOME = 0;

    /** 支出模式 */
    public static final int MODE_EXPENSE = 1;

    private final Player player;

    /** 被动收入计时器 */
    private int passiveIncomeTimer = 0;

    /** 当前模式：0=收入, 1=支出 */
    private int currentMode = MODE_INCOME;

    /**
     * 构造函数
     */
    public AccountantPlayerComponent(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.passiveIncomeTimer = tag.getInt("PassiveIncomeTimer");
        this.currentMode = tag.getInt("CurrentMode");
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("PassiveIncomeTimer", this.passiveIncomeTimer);
        tag.putInt("CurrentMode", this.currentMode);
    }

    @Override
    public void reset() {
        this.passiveIncomeTimer = 0;
        this.currentMode = MODE_INCOME;
        sync();
    }

    @Override
    public void clear() {
        this.reset();
    }

    @Override
    public void serverTick() {
        // 处理被动收入 - 只在游戏开始后且玩家是会计时生效
        if (passiveIncomeTimer > 0) {
            passiveIncomeTimer--;
        } else {
            // 检查游戏是否正在运行
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
            if (gameWorldComponent != null && gameWorldComponent.isRunning()) {
                // 检查玩家是否是会计角色
                if (gameWorldComponent.isRole(player, ModRoles.ACCOUNTANT)) {
                    // 给予被动收入
                    givePassiveIncome();
                }
            }
            passiveIncomeTimer = PASSIVE_INCOME_INTERVAL;
        }
    }

    /**
     * 给予被动收入
     */
    private void givePassiveIncome() {
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        PlayerShopComponent shopComponent = PlayerShopComponent.KEY.get(player);
        shopComponent.balance += PASSIVE_INCOME_AMOUNT;
        shopComponent.sync();

        serverPlayer.displayClientMessage(
                Component.translatable("message.noellesroles.accountant.passive_income", PASSIVE_INCOME_AMOUNT)
                        .withStyle(ChatFormatting.GOLD),
                true);
    }

    /**
     * 切换模式（蹲下按技能键）
     */
    public void toggleMode() {
        if (currentMode == MODE_INCOME) {
            currentMode = MODE_EXPENSE;
        } else {
            currentMode = MODE_INCOME;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            Component message;
            if (currentMode == MODE_INCOME) {
                message = Component.translatable("message.noellesroles.accountant.mode.income")
                        .withStyle(ChatFormatting.GOLD);
            } else {
                message = Component.translatable("message.noellesroles.accountant.mode.expense")
                        .withStyle(ChatFormatting.AQUA);
            }
            serverPlayer.displayClientMessage(message, true);
        }

        sync();
    }

    /**
     * 使用技能（直接按技能键）
     */
    public void useAbility() {
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        // 检查金币是否足够
        PlayerShopComponent shopComponent = PlayerShopComponent.KEY.get(player);
        if (shopComponent.balance < SKILL_COST) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.accountant.insufficient_funds", SKILL_COST)
                            .withStyle(ChatFormatting.RED),
                    true);
            return;
        }

        // 扣除金币
        shopComponent.balance -= SKILL_COST;
        shopComponent.sync();

        // 根据当前模式执行技能
        if (currentMode == MODE_INCOME) {
            executeIncomeSkill(serverPlayer);
        } else {
            executeExpenseSkill(serverPlayer);
        }
    }

    /**
     * 执行收入模式技能
     * 查看准星对准的玩家的金币量是否超过300
     */
    private void executeIncomeSkill(ServerPlayer serverPlayer) {
        // 播放钟的声音
        serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 0.5F, 1.0F);

        // 获取准星对准的玩家
        Player target = getTargetPlayer(serverPlayer);
        if (target == null) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.accountant.no_target")
                            .withStyle(ChatFormatting.RED),
                    true);
            return;
        }

        // 检查目标玩家金币数量
        PlayerShopComponent targetShop = PlayerShopComponent.KEY.get(target);
        int targetBalance = targetShop.balance;

        if (targetBalance >= INCOME_QUERY_THRESHOLD) {
            // 金币超过300，给予提示
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.accountant.income.rich", target.getDisplayName(), targetBalance)
                            .withStyle(ChatFormatting.GREEN),
                    true);
        } else {
            // 金币未超过300，给予提示
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.accountant.income.poor", target.getDisplayName(), targetBalance)
                            .withStyle(ChatFormatting.YELLOW),
                    true);
        }
    }

    /**
     * 执行支出模式技能
     * 查看半径4格内玩家在30秒内总支出金币数量的大致范围
     */
    private void executeExpenseSkill(ServerPlayer serverPlayer) {
        // 播放翻书声
        serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);

        // 获取半径4格内的玩家
        java.util.List<ServerPlayer> nearbyPlayers = serverPlayer.level().players().stream()
                .filter(p -> p != serverPlayer && !p.isSpectator() && p.distanceTo(serverPlayer) <= EXPENSE_QUERY_RADIUS)
                .filter(p -> p instanceof ServerPlayer)
                .map(p -> (ServerPlayer) p)
                .toList();

        if (nearbyPlayers.isEmpty()) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.accountant.no_nearby_players")
                            .withStyle(ChatFormatting.RED),
                    true);
            return;
        }

        // TODO: 会计的支出技能，计算游戏时间30s内4格半径内玩家消耗的金币数量区间
        int totalExpense = 0;
        for (Player nearby : nearbyPlayers) {
            // 模拟：使用一个随机值来模拟支出（实际应该记录真实支出）
            // 这里简化为：根据玩家金币量给出一个估计范围
            PlayerShopComponent nearbyShop = PlayerShopComponent.KEY.get(nearby);
            // 使用伪随机但确定性的方法生成估计值
            int estimatedExpense = (nearbyShop.balance / 3) % 500;
            totalExpense += estimatedExpense;
        }

        // 计算整百区间
        int lowerBound = (totalExpense / 100) * 100;
        int upperBound = lowerBound + 100;

        // 给予会计提示
        serverPlayer.displayClientMessage(
                Component.translatable("message.noellesroles.accountant.expense.result", nearbyPlayers.size(), lowerBound, upperBound)
                        .withStyle(ChatFormatting.AQUA),
                true);
    }

    /**
     * 获取准星对准的玩家
     */
    private Player getTargetPlayer(ServerPlayer player) {
        double minDistance = 5.0;
        Player target = null;

        for (Player otherPlayer : player.level().players()) {
            if (otherPlayer.isSpectator())
                continue;
            if (otherPlayer.getUUID().equals(player.getUUID()))
                continue;

            double distance = player.distanceTo(otherPlayer);
            if (distance <= minDistance) {
                // 检查是否在准星方向
                net.minecraft.world.phys.Vec3 eyePos = player.getEyePosition();
                net.minecraft.world.phys.Vec3 lookVec = player.getLookAngle().normalize();
                net.minecraft.world.phys.Vec3 toTarget = otherPlayer.position().subtract(eyePos).normalize();
                double dotProduct = lookVec.dot(toTarget);

                if (dotProduct > 0.8) {
                    if (target == null || distance < player.distanceTo(target)) {
                        target = otherPlayer;
                    }
                }
            }
        }

        return target;
    }

    /**
     * 获取当前模式
     */
    public int getCurrentMode() {
        return currentMode;
    }

    /**
     * 获取被动收入剩余时间（秒）
     */
    public int getPassiveIncomeRemainingSeconds() {
        return (passiveIncomeTimer + 19) / 20;
    }

    /**
     * 同步组件数据到客户端
     */
    private void sync() {
        if (!player.level().isClientSide) {
            KEY.sync(player);
        }
    }
}
