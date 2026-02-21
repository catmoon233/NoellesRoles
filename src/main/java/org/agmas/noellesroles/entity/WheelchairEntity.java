package org.agmas.noellesroles.entity;

import java.util.ArrayList;
import java.util.List;

import org.agmas.noellesroles.ModItems;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WheelchairEntity extends Mob {
    // 在 WheelchairEntity 类中添加字段
    private float rotationVelocity = 0.0f;
    private static final float ROTATION_ACCELERATION = 2f; // 旋转加速度（每 tick 速度变化）
    private static final float ROTATION_FRICTION = 0.4f; // 旋转摩擦（每 tick 速度衰减系数）
    private static final float MAX_ROTATION_SPEED = 10.0f; // 最大旋转速度（角度/tick）
    private ItemStack item = ItemStack.EMPTY;
    // 新增：前进速度相关
    private float forwardSpeed = 0.0f; // 当前沿朝向的速度（正前负后）
    private GameWorldComponent gameWorldComponent;
    private static final float FORWARD_ACCELERATION = 0.02f; // 每 tick 速度增量
    private static final float FORWARD_FRICTION = 0.8f; // 无输入时每 tick 乘系数
    // 最大速度直接使用属性 Attributes.MOVEMENT_SPEED 的值

    public WheelchairEntity(EntityType<? extends Mob> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public void addPassenger(Entity entity) {
        super.addPassenger(entity);
        entity.setYRot(this.getYRot());
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) { // 只在服务端处理
            // ---------- 1. 撞到方块：水平速度归零 ----------
            if (this.horizontalCollision) {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(0, motion.y, 0);
                this.forwardSpeed = 0;
            }

            // ---------- 2. 撞到玩家：击退 ----------
            // 轮椅当前水平速度足够大时才触发（避免慢速蹭到也击飞）
            double speedSqr = this.getDeltaMovement().horizontalDistanceSqr();
            if (speedSqr > 0.02) { // 速度平方 > 0.02 ≈ 速度 > 0.14
                // 扩大碰撞箱检测周围玩家（排除驾驶员）
                AABB searchBox = this.getBoundingBox().inflate(0.3);
                List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, searchBox,
                        p -> p != this.getControllingPassenger() && p.isAlive());

                for (Player player : nearbyPlayers) {
                    // 击退方向：从轮椅指向玩家（或沿着轮椅运动方向）
                    Vec3 knockbackDir = player.position().subtract(this.position()).normalize();
                    // 击退力度：基于轮椅速度，可调整系数
                    double knockbackStrength = Math.sqrt(speedSqr) * 2.5;
                    player.setDeltaMovement(player.getDeltaMovement().add(knockbackDir.scale(knockbackStrength)));
                    player.hurtMarked = true; // 标记速度需要同步到客户端

                    // 可选：轮椅因碰撞减速（模拟反作用力）
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.6));
                }
            }
        }
    }

    @Override
    public LivingEntity getControllingPassenger() {
        // 返回第一个乘客（玩家）作为控制者
        if (!this.getPassengers().isEmpty()) {
            return (this.getPassengers().get(0) instanceof Player player) ? player : null;
        }
        return null;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            // 计算你希望乘客坐的位置（相对于实体脚部坐标）
            // - Y 偏移：降低到轮椅座椅高度（例如 0.3）
            // - Z 偏移：向后移动一小段距离（例如 -0.2，即实体后方）
            // - X 偏移：如果需要左右调整，也可以添加（例如 0.0）
            double offsetY = -0.1; // 垂直降低
            double offsetZ = -0.2; // 向后移动（负Z为后方）
            double offsetX = 0.0; // 水平居中

            // 计算目标位置：实体坐标 + 偏移量（注意旋转：偏移量应基于实体的朝向）
            // 如果你希望偏移量始终相对于实体的前后方向（即无论实体朝向如何，乘客始终坐在后方），
            // 需要将偏移向量按实体的旋转进行变换。
            // 简单情况下，如果偏移是相对于世界坐标（即固定向后），则直接相加即可。
            // 但通常“向后”指的是实体的后方，所以需要旋转。
            Vec3 offset = new Vec3(offsetX, offsetY, offsetZ)
                    .yRot(-this.getYRot() * (float) Math.PI / 180.0F); // 应用实体旋转

            Vec3 targetPos = this.position().add(offset);

            // 使用 moveFunction 设置乘客位置（这会自动处理平滑移动）
            moveFunction.accept(passenger, targetPos.x, targetPos.y, targetPos.z);
        }
    }

    @Override
    public void travel(Vec3 movementInput) {
        if (this.getControllingPassenger() instanceof Player player) {
            float forward = player.zza;
            float strafe = player.xxa;

            // 处理旋转速度（粘稠感）
            if (strafe != 0) {
                // 有输入：加速到目标速度（方向由 strafe 决定）
                float targetSpeed = strafe * MAX_ROTATION_SPEED; // strafe 为 -1 到 1，所以 targetSpeed 为 -15 到 15
                // 逐渐接近目标速度（加速）
                if (rotationVelocity < targetSpeed) {
                    rotationVelocity = Math.min(rotationVelocity + ROTATION_ACCELERATION, targetSpeed);
                } else if (rotationVelocity > targetSpeed) {
                    rotationVelocity = Math.max(rotationVelocity - ROTATION_ACCELERATION, targetSpeed);
                }
            } else {
                // 无输入：摩擦减速
                rotationVelocity *= ROTATION_FRICTION;
                // 如果速度很小，直接归零避免永远不归零
                if (Math.abs(rotationVelocity) < 0.01f) {
                    rotationVelocity = 0.0f;
                }
            }

            // 应用旋转
            if (rotationVelocity != 0) {
                this.setYRot(this.getYRot() - rotationVelocity);
                player.setYRot(player.getYRot() - rotationVelocity);
                // 注意正负：原本是 -strafe*rotationSpeed，这里 rotationVelocity
                // 已经包含了方向
                this.yRotO = this.getYRot();
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.getYRot();
            }

            // ===== 前进/后退惯性（新增） =====
            float maxSpeed = (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
            if (forward != 0) {
                float targetSpeed = forward * maxSpeed; // 目标速度（带方向）
                // 加速逼近目标速度
                if (forwardSpeed < targetSpeed) {
                    forwardSpeed = Math.min(forwardSpeed + FORWARD_ACCELERATION, targetSpeed);
                } else if (forwardSpeed > targetSpeed) {
                    forwardSpeed = Math.max(forwardSpeed - FORWARD_ACCELERATION, targetSpeed);
                }
            } else {
                // 无输入：摩擦减速
                forwardSpeed *= FORWARD_FRICTION;
                if (Math.abs(forwardSpeed) < 0.001f) {
                    forwardSpeed = 0.0f;
                }
            }

            // 根据当前 forwardSpeed 和实体朝向设置水平速度
            if (forwardSpeed != 0) {
                Vec3 forwardVec = Vec3.directionFromRotation(0, this.getYRot()).scale(forwardSpeed);
                this.setDeltaMovement(forwardVec.x, this.getDeltaMovement().y, forwardVec.z);
            } else {
                // 速度为0时，停止水平运动（但保留垂直速度）
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(0, motion.y, 0);
            }

            // 3. 处理跳跃（可选）：玩家按空格让轮椅跳跃？如果需要可以添加
            // 4. 更新实体位置（必须调用，否则不会移动）
            super.travel(movementInput);
            this.move(MoverType.SELF, this.getDeltaMovement());
        } else {
            // 无乘客时的默认行为（例如自由落体、掉落等）
            super.travel(movementInput);
        }
    }

    @Override
    public float maxUpStep() {
        float f = 0.6F;
        if (gameWorldComponent == null) {
            var gameComp = GameWorldComponent.KEY.maybeGet(this.level()).orElse(null);
            if (gameComp != null) {
                this.gameWorldComponent = gameComp;
            } else {
                return 0.6F;
            }
        }
        if (gameWorldComponent.isJumpAvailable()) {
            f = 1F;
        }
        return this.getControllingPassenger() instanceof Player ? Math.max(f, 0.1F) : f;
    }

    // 注册属性（速度、生命等）
    public static AttributeSupplier.Builder createAttributes() {

        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.STEP_HEIGHT, 0.5);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // 1. 如果玩家正在潜行，尝试回收为物品
        if (player.isShiftKeyDown()) {
            if (!this.level().isClientSide) {
                // 回收逻辑：给玩家一个轮椅物品，并移除实体
                ItemStack wheelchairItem = new ItemStack(ModItems.WHEELCHAIR); // 你需要一个自定义物品
                if (!player.getInventory().add(wheelchairItem)) {
                    // 如果背包满了，掉落在地上
                    player.drop(wheelchairItem, false);
                }
                this.discard(); // 移除实体
            }
            return InteractionResult.SUCCESS; // 客户端返回 SUCCESS 以播放动画
        }

        // 2. 否则，如果实体没有被乘客且玩家非潜行，则让玩家骑乘
        if (this.getPassengers().isEmpty() && !player.isShiftKeyDown()) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.isCreativePlayer()) {
            this.discard();
            return true;
        }
        return false; // 完全无敌
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        var arr = new ArrayList<ItemStack>();
        arr.add(this.item);
        return arr;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        return this.item;
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.LEFT;
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        this.item = itemStack;
    }
}