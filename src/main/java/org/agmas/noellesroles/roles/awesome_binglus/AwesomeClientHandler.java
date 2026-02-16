package org.agmas.noellesroles.roles.awesome_binglus;

import org.agmas.noellesroles.component.AwesomePlayerComponent;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.player.Player;

public class AwesomeClientHandler {

    public static void renderParticleOfPlayer(Minecraft client, Player p, AwesomePlayerComponent aweC) {
        DustParticleOptions greenDust = new DustParticleOptions(
                new Vector3f(1.0f
                        * ((float) aweC.nearByDeathTime / (float) AwesomePlayerComponent.nearByDeathTimeRecordTime),
                        0.0f,
                        0.0f),
                2.0f * ((float) aweC.nearByDeathTime / (float) AwesomePlayerComponent.nearByDeathTimeRecordTime));
        client.level.addParticle(
                greenDust, true,
                p.getX(), p.getY() + 1, p.getZ(), // 在玩家脚部稍上方
                0, 0, 0 // 速度为零
        );
    }

}
