package org.agmas.noellesroles;

import dev.doctor4t.ratatouille.util.registrar.SoundEventRegistrar;
import net.minecraft.sounds.SoundEvent;

public class NRSounds {
    public static final SoundEventRegistrar registrar = new SoundEventRegistrar(Noellesroles.MOD_ID);
    public static final SoundEvent GAMBER_DEATH = registrar.create("noellesroles.gamber_died");
    public static final SoundEvent GONGXI_FACAI = registrar.create("noellesroles.gongxifacai");
    public static final SoundEvent HARPY_WELCOME = registrar.create("noellesroles.harpy_welcome");
    public static final SoundEvent WIND = registrar.create("noellesroles.wind");
    public static void initialize() {
        registrar.registerEntries();
    }
}

