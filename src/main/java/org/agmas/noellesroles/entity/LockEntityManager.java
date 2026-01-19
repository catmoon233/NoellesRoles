package org.agmas.noellesroles.entity;

import net.minecraft.core.Vec3i;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * 锁实体管理器单例
 * - 管理锁实体与门之间的关系
 * - 使用方式：使用撬锁器开门时查询是否被锁实体影响
 * - TODO : 该管理器并不会在关闭地图(也可能是游戏？)后保存数据，总之就是重启之后所有锁实体将不会影响到门，但是考虑到游戏也不会在关闭后继续，所以没必要管
 */
public class LockEntityManager {
    private LockEntityManager() {};
    public static LockEntityManager getInstance(){
        return instance;
    }
    /** 获取对应位置的锁实体*/
    public LockEntity getLockEntity(Vec3i pos) {
        if (lockEntities.containsKey(pos)) {
            return lockEntities.get(pos).peek();
        }
        return null;
    }

    /** 添加锁实体*/
    public void addLockEntity(Vec3i pos, LockEntity lockEntity) {
        if(!lockEntities.containsKey(pos))
        {
            Stack<LockEntity> stack = new Stack<>();
            lockEntities.put(pos, stack);
        }
        lockEntities.get(pos).push(lockEntity);
    }

    /** 移除锁实体*/
    public void removeLockEntity(Vec3i pos, LockEntity lockEntity) {
        if (lockEntities.containsKey(pos)) {
            lockEntities.get(pos).remove(lockEntity);
            if (lockEntities.get(pos).isEmpty()) {
                lockEntities.remove(pos);
            }
        }
    }

    private static final LockEntityManager instance = new LockEntityManager();
    // 使用位置与锁列表对应：每个格子可以匹配多个锁实体
    private static final Map<Vec3i, Stack<LockEntity>> lockEntities = new HashMap<>();
}
