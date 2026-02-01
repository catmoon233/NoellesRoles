package org.agmas.noellesroles.mixin.client.manipulator;

import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.manipulator.ManipulatorPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {

//    @Shadow
//    private Entity entity;
//
//    @Shadow
//    private float eyeHeightOld;
//
//    @Shadow
//    private float eyeHeight;
//
//    private static boolean change = false;
//    @Inject(method = "setup", at = @At("HEAD"))
//    public void onSetup(BlockGetter level, Entity newFocusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
//        final var instance = Minecraft.getInstance();
//        LocalPlayer player = instance.player;
//
//        if (TMMClient.gameComponent!=null && TMMClient.gameComponent.isRunning() && TMMClient.isPlayerAliveAndInSurvival() && TMMClient.gameComponent.isRole(player, ModRoles.MANIPULATOR)){
//            final var manipulatorPlayerComponent = ManipulatorPlayerComponent.KEY.get(player);
//            if (manipulatorPlayerComponent.isControlling) {
//                if (manipulatorPlayerComponent.target != null){
//                    Player target = instance.level.getPlayerByUUID(manipulatorPlayerComponent.target);
//                    if (target != null) {
//                        Camera camera = (Camera) (Object) this;
//                        instance.options.setCameraType(CameraType.THIRD_PERSON_BACK);
//                        change = true;
//                        camera.getEntity().setPos(target.getX(), target.getEyeHeight(), target.getZ());
//                        camera.getEntity().setYRot(target.getYRot());
//                        camera.getEntity().setXRot(target.getXRot());
//                    }if (change){
//                        instance.options.setCameraType(CameraType.FIRST_PERSON);
//                        change = false;
//                    }
//                }if (change){
//                    instance.options.setCameraType(CameraType.FIRST_PERSON);
//                    change = false;
//                }
//            }if (change){
//                instance.options.setCameraType(CameraType.FIRST_PERSON);
//                change = false;
//            }
//        }else if (change){
//            instance.options.setCameraType(CameraType.FIRST_PERSON);
//            change = false;
//        }
//
//
//    }
}