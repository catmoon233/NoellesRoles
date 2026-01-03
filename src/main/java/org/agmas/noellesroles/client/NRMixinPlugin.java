package org.agmas.noellesroles.client;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class NRMixinPlugin implements IMixinConfigPlugin {
    public void onLoad(String mixinPackage) {
        System.out.println("Noelle's Roles: Mixin Plugin Loaded");
    }

    public String getRefMapperConfig() {
        return "";
    }

    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return !"dpm.harpysimpleroles.mixin.client.LimitedInventoryScreen".equals(mixinClassName) && !"dpm.harpysimpleroles.mixin.PurchaseMixin".equals(mixinClassName) && !"dev.doctor4t.ratatouille.mixin.client.armor.PlayerEntityRendererMixin".equals(mixinClassName) && !"dev.doctor4t.ratatouille.mixin.client.armor.LivingEntityRendererMixin".equals(mixinClassName);
    }

    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    public List<String> getMixins() {
        return List.of();
    }

    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
