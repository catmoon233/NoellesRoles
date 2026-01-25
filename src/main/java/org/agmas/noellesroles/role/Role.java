package org.agmas.noellesroles.role;

import net.minecraft.resources.ResourceLocation;

public class Role extends dev.doctor4t.trainmurdermystery.api.Role {

    public Role(ResourceLocation identifier, int color, boolean isInnocent, boolean canUseKiller, MoodType moodType,
            int maxSprintTime, boolean canSeeTime) {
        super(identifier, color, isInnocent, canUseKiller, moodType, maxSprintTime, canSeeTime);
    }
    
}
