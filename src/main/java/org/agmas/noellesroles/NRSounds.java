package org.agmas.noellesroles;

import dev.doctor4t.ratatouille.util.registrar.SoundEventRegistrar;
import net.minecraft.sounds.SoundEvent;

public class NRSounds {
    public static final SoundEventRegistrar registrar = new SoundEventRegistrar(Noellesroles.MOD_ID);
    public static final SoundEvent GAMBER_DEATH = registrar.create("gamber_death");
    public static void initialize() {
        registrar.registerEntries();
    }
}
