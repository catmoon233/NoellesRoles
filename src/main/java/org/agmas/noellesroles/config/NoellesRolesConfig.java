package org.agmas.noellesroles.config;

import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;

import java.util.List;

public class NoellesRolesConfig {
    public static ConfigClassHandler<NoellesRolesConfig> HANDLER = ConfigClassHandler.createBuilder(NoellesRolesConfig.class)
            .id(Identifier.of(Noellesroles.MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve( Noellesroles.MOD_ID + ".json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry(comment = "Whether insane players will randomly see people as morphed.")
    public boolean insanePlayersSeeMorphs = true;
    @SerialEntry(comment = "Allows the shitpost roles to retain their disable/enable state after a server restart")
    public boolean shitpostRoles = false;

    @SerialEntry(comment = "Starting cooldown (in ticks)")
    public int generalCooldownTicks = GameConstants.getInTicks(0,30);

    @SerialEntry(comment = "Allow Natural deaths to trigger voodoo (deaths without an assigned killer)")
    public boolean voodooNonKillerDeaths = false;

    @SerialEntry(comment = "Maximum number of Conductors allowed")
    public int conductorMax = 1;
    @SerialEntry(comment = "Maximum number of Executioners allowed")
    public int executionerMax = 1;
    @SerialEntry(comment = "Maximum number of Vultures allowed")
    public int vultureMax = 1;
    @SerialEntry(comment = "Maximum number of Jesters allowed")
    public int jesterMax = 1;
    @SerialEntry(comment = "Maximum number of Morphlings allowed")
    public int morphlingMax = 1;
    @SerialEntry(comment = "Maximum number of Bartenders allowed")
    public int bartenderMax = 1;
    @SerialEntry(comment = "Maximum number of Noisemakers allowed")
    public int noisemakerMax = 1;
    @SerialEntry(comment = "Maximum number of Phantoms allowed")
    public int phantomMax = 1;
    @SerialEntry(comment = "Maximum number of Awesome Bingluses allowed")
    public int awesomeBinglusMax = 1;
    @SerialEntry(comment = "Maximum number of Swappers allowed")
    public int swapperMax = 1;
    @SerialEntry(comment = "Maximum number of Voodoos allowed")
    public int voodooMax = 1;
    @SerialEntry(comment = "Maximum number of Coroners allowed")
    public int coronerMax = 1;
    @SerialEntry(comment = "Maximum number of Recallers allowed")
    public int recallerMax = 1;

    // Skills configuration
    @SerialEntry(comment = "Morphling - Morph duration in seconds")
    public int morphlingMorphDuration = 35;
    @SerialEntry(comment = "Morphling - Morph cooldown in seconds")
    public int morphlingMorphCooldown = 20;
    
    @SerialEntry(comment = "Bartender - Glow duration in seconds")
    public int bartenderGlowDuration = 40;
    
    @SerialEntry(comment = "Recaller - Maximum recall distance in blocks")
    public int recallerMaxDistance = 50;
    
    @SerialEntry(comment = "Vulture - Bodies required to win")
    public int vultureBodiesRequired = 2;
    
    @SerialEntry(comment = "Jester - Time to complete jest in seconds")
    public int jesterJestTime = 60;
    
    @SerialEntry(comment = "Jester - Maximum psycho ticks before death")
    public int jesterMaxPsychoTicks = 44;
    
    @SerialEntry(comment = "Recaller - Recall mark cooldown in seconds")
    public int recallerMarkCooldown = 10;
    
    @SerialEntry(comment = "Recaller - Teleport cooldown in seconds")
    public int recallerTeleportCooldown = 30;
    
    @SerialEntry(comment = "Phantom - Invisibility duration in seconds")
    public int phantomInvisibilityDuration = 30;
    
    @SerialEntry(comment = "Phantom - Invisibility cooldown in seconds")
    public int phantomInvisibilityCooldown = 90;
    
    @SerialEntry(comment = "Voodoo - Voodoo ritual cooldown in seconds")
    public int voodooCooldown = 30;
    
    @SerialEntry(comment = "Vulture - Eat body cooldown in seconds")
    public int vultureEatCooldown = 20;
    
    @SerialEntry(comment = "Executioner - Knife cooldown in seconds")
    public int executionerKnifeCooldown = 10;
    
    @SerialEntry(comment = "Swapper - Swap cooldown in seconds")
    public int swapperSwapCooldown = 60;
}