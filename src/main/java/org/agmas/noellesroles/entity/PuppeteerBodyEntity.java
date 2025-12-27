package org.agmas.noellesroles.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.PuppeteerPlayerComponent;

import java.util.Optional;
import java.util.UUID;

/**
 * 傀儡本体实体
 * 
 * 当傀儡师使用假人技能时，本体会在原位置生成一个本体实体。
 * 这个实体使用玩家的模型和皮肤，可以被攻击。
 * 如果本体被杀死，傀儡师也会死亡。
 */
public class PuppeteerBodyEntity extends LivingEntity {
    
    /** 所有者 UUID */
    private static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(
        PuppeteerBodyEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID
    );
    
    /** 皮肤 GameProfile（用于渲染玩家皮肤） */
    private GameProfile skinProfile = null;
    
    /** 所有者玩家名称 */
    private String ownerName = "";
    
    /** 最大存活时间（10分钟 = 12000 tick），防止无限存在 */
    public static final int MAX_LIFETIME = 12000;
    
    /** 存活时间计数器 */
    private int lifetime = 0;
    
    /** 所有者玩家引用（缓存） */
    private PlayerEntity ownerCache = null;
    
    public PuppeteerBodyEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(false); // 有重力
        this.setHealth(20.0F); // 20点生命值（和玩家一样）
    }
    
    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(OWNER_UUID, Optional.empty());
    }
    
    /**
     * 设置所有者
     */
    public void setOwner(PlayerEntity owner) {
        if (owner != null) {
            this.dataTracker.set(OWNER_UUID, Optional.of(owner.getUuid()));
            this.ownerCache = owner;
            this.ownerName = owner.getName().getString();
            
            // 设置皮肤（获取玩家的 GameProfile）
            if (owner instanceof ServerPlayerEntity serverPlayer) {
                this.skinProfile = serverPlayer.getGameProfile();
            }
            
            // 设置自定义名称
            this.setCustomName(Text.literal(owner.getName().getString() + " 的本体"));
            this.setCustomNameVisible(false);
        }
    }
    
    /**
     * 获取所有者 UUID
     */
    public Optional<UUID> getOwnerUuid() {
        return this.dataTracker.get(OWNER_UUID);
    }
    
    /**
     * 获取所有者玩家
     */
    public PlayerEntity getOwner() {
        if (ownerCache != null && ownerCache.isAlive()) {
            return ownerCache;
        }
        
        Optional<UUID> ownerUuid = getOwnerUuid();
        if (ownerUuid.isPresent()) {
            ownerCache = getWorld().getPlayerByUuid(ownerUuid.get());
            return ownerCache;
        }
        return null;
    }
    
    /**
     * 获取皮肤 GameProfile（用于客户端渲染）
     */
    public GameProfile getSkinProfile() {
        return skinProfile;
    }
    
    /**
     * 获取所有者名称
     */
    public String getOwnerName() {
        return ownerName;
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (getWorld().isClient()) return;
        
        // 增加存活时间
        lifetime++;
        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }
        
        // 检查所有者是否还存在
        PlayerEntity owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }
    }
    
    @Override
    public boolean damage(DamageSource source, float amount) {
        if (getWorld().isClient()) return false;
        
        // 调用父类处理伤害
        boolean result = super.damage(source, amount);
        
        // 如果死亡，通知傀儡师
        if (this.isDead()) {
            PlayerEntity owner = getOwner();
            if (owner != null) {
                // 通知傀儡师组件本体死亡
                PuppeteerPlayerComponent puppeteerComp =
                    ModComponents.PUPPETEER.get(owner);
                puppeteerComp.onBodyDeath();
            }
        }
        
        return result;
    }
    
    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        
        // 确保通知傀儡师
        PlayerEntity owner = getOwner();
        if (owner != null) {
            PuppeteerPlayerComponent puppeteerComp =
                ModComponents.PUPPETEER.get(owner);
            puppeteerComp.onBodyDeath();
        }
    }
    
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        
        if (nbt.contains("OwnerUUID")) {
            this.dataTracker.set(OWNER_UUID, Optional.of(nbt.getUuid("OwnerUUID")));
        }
        if (nbt.contains("OwnerName")) {
            this.ownerName = nbt.getString("OwnerName");
        }
        // SkinProfile 通过 OwnerUUID 在客户端动态获取，不需要从 NBT 加载
        this.lifetime = nbt.contains("Lifetime") ? nbt.getInt("Lifetime") : 0;
    }
    
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        
        Optional<UUID> ownerUuid = getOwnerUuid();
        ownerUuid.ifPresent(uuid -> nbt.putUuid("OwnerUUID", uuid));
        nbt.putString("OwnerName", this.ownerName);
        // SkinProfile 通过 OwnerUUID 在客户端动态获取，不需要保存到 NBT
        nbt.putInt("Lifetime", this.lifetime);
    }
    
    @Override
    public boolean canHit() {
        return true; // 可以被击中
    }
    
    @Override
    public boolean isPushable() {
        return false; // 不能被推动
    }
    
    @Override
    public Iterable<net.minecraft.item.ItemStack> getArmorItems() {
        return java.util.Collections.emptyList();
    }
    
    @Override
    public net.minecraft.item.ItemStack getEquippedStack(net.minecraft.entity.EquipmentSlot slot) {
        return net.minecraft.item.ItemStack.EMPTY;
    }
    
    @Override
    public void equipStack(net.minecraft.entity.EquipmentSlot slot, net.minecraft.item.ItemStack stack) {
        // 不装备任何物品
    }
    
    @Override
    public net.minecraft.util.Arm getMainArm() {
        return net.minecraft.util.Arm.RIGHT;
    }
}