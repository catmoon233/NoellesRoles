package org.agmas.noellesroles.init;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.events.GameInitializeEvent;
import org.agmas.harpymodloader.modded_murder.RoleAssignmentManager;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.role.ModRoles;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.constants.SERoles;

public class InitModRolesMax {
    public static int SPLIT_PERSONALITY_CHANCE = 10; // 10 in 100
    public static int REFUGEE_CHANCE = 10; // 10 in 100

    public static void registerStatics() {

        // 设置角色最大数量
        Harpymodloader.setRoleMaximum(ModRoles.POISONER_ID, 0);
        // 和医生一起生成
        Harpymodloader.setRoleMaximum(ModRoles.DOCTOR_ID, 0);
        Harpymodloader.setRoleMaximum(ModRoles.ATTENDANT_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.CORONER_ID, 1);

        // 同时出现
        RoleAssignmentManager.addOccupationRole(ModRoles.POISONER, ModRoles.DOCTOR);

        Harpymodloader.setRoleMaximum(ModRoles.CONDUCTOR_ID, NoellesRolesConfig.HANDLER.instance().conductorMax);
        Harpymodloader.setRoleMaximum(ModRoles.MANIPULATOR, 0);
        Harpymodloader.setRoleMaximum(ModRoles.EXECUTIONER_ID, NoellesRolesConfig.HANDLER.instance().executionerMax);
        Harpymodloader.setRoleMaximum(ModRoles.VULTURE_ID, NoellesRolesConfig.HANDLER.instance().vultureMax);
        Harpymodloader.setRoleMaximum(ModRoles.JESTER_ID, NoellesRolesConfig.HANDLER.instance().jesterMax);
        Harpymodloader.setRoleMaximum(ModRoles.MORPHLING_ID, NoellesRolesConfig.HANDLER.instance().morphlingMax);
        Harpymodloader.setRoleMaximum(ModRoles.BARTENDER_ID, NoellesRolesConfig.HANDLER.instance().bartenderMax);
        Harpymodloader.setRoleMaximum(ModRoles.NOISEMAKER_ID, NoellesRolesConfig.HANDLER.instance().noisemakerMax);
        Harpymodloader.setRoleMaximum(ModRoles.PHANTOM_ID, NoellesRolesConfig.HANDLER.instance().phantomMax);
        Harpymodloader.setRoleMaximum(ModRoles.AWESOME_BINGLUS_ID,
                NoellesRolesConfig.HANDLER.instance().awesomeBinglusMax);
        Harpymodloader.setRoleMaximum(ModRoles.SWAPPER_ID, NoellesRolesConfig.HANDLER.instance().swapperMax);
        Harpymodloader.setRoleMaximum(ModRoles.VOODOO_ID, NoellesRolesConfig.HANDLER.instance().voodooMax);
        Harpymodloader.setRoleMaximum(ModRoles.CORONER_ID, NoellesRolesConfig.HANDLER.instance().coronerMax);
        Harpymodloader.setRoleMaximum(ModRoles.RECALLER_ID, NoellesRolesConfig.HANDLER.instance().recallerMax);
        Harpymodloader.setRoleMaximum(ModRoles.BROADCASTER_ID, NoellesRolesConfig.HANDLER.instance().broadcasterMax);
        Harpymodloader.setRoleMaximum(ModRoles.GAMBLER_ID, NoellesRolesConfig.HANDLER.instance().gamblerMax);
        Harpymodloader.setRoleMaximum(ModRoles.GLITCH_ROBOT_ID, NoellesRolesConfig.HANDLER.instance().glitchRobotMax);
        Harpymodloader.setRoleMaximum(ModRoles.GHOST_ID, NoellesRolesConfig.HANDLER.instance().ghostMax);
        Harpymodloader.setRoleMaximum(ModRoles.THIEF_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.BANDIT_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.SHERIFF_ID, NoellesRolesConfig.HANDLER.instance().sheriffMax);
        Harpymodloader.setRoleMaximum(ModRoles.BOMBER_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.OLDMAN_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.CHEF_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.FORTUNETELLER_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.WIND_YAOSE_ID, 1);
    }

    public static void registerDynamic() {
        GameInitializeEvent.EVENT.register((serverLevel, gameWorldComponent, players) -> {
            final int players_count = serverLevel.getServer().getPlayerCount();
            {
                // 杀手中立
                var neutralRoles = new ArrayList<Role>(TMMRoles.ROLES.values());
                neutralRoles.removeIf((r) -> {
                    if (r.isNeutrals() && r.isNeutralForKiller())
                        return false;
                    return true;
                });
                Collections.shuffle(neutralRoles);
                for (var r : neutralRoles) {
                    Harpymodloader.setRoleMaximum(r, 0);
                }
                int neutralForKillers = 0;
                neutralForKillers = players_count / 6;
                for (int i = 0; i < neutralForKillers && i < neutralRoles.size(); i++) {
                    Harpymodloader.setRoleMaximum(neutralRoles.get(i), 1);
                }
            }
            // 动态大小
            // 年兽角色：5%概率生成
            Random random = new Random();
            if (players_count >= 10 && random.nextInt(0, 100) >= 25) {
                Harpymodloader.setRoleMaximum(ModRoles.WAYFARER_ID, 1);
            } else {
                Harpymodloader.setRoleMaximum(ModRoles.WAYFARER_ID, 0);
            }
            if (random.nextInt(0, 100) < 50) {
                Harpymodloader.setRoleMaximum(ModRoles.POISONER_ID, 1);
            } else {
                Harpymodloader.setRoleMaximum(ModRoles.POISONER_ID, 0);
            }
            if (random.nextInt(0, 100) <= 25) {
                Harpymodloader.setRoleMaximum(ModRoles.MAGICIAN_ID, 1);
            } else {
                Harpymodloader.setRoleMaximum(ModRoles.MAGICIAN_ID, 0);
            }
            if (random.nextInt(0, 100) <= 50) {
                Harpymodloader.setRoleMaximum(SERoles.NECROMANCER, 1);
            } else {
                Harpymodloader.setRoleMaximum(SERoles.NECROMANCER, 0);
            }
            if (random.nextInt(0, 100) <= 75) {
                Harpymodloader.setRoleMaximum(ModRoles.MONITOR_ID, 1);
            } else {
                Harpymodloader.setRoleMaximum(ModRoles.MONITOR_ID, 0);
            }
            if (random.nextInt(0, 100) < 20) {
                Harpymodloader.setRoleMaximum(ModRoles.NIAN_SHOU_ID, 1);
            } else {
                Harpymodloader.setRoleMaximum(ModRoles.NIAN_SHOU_ID, 0);
            }
            if (players_count >= 10) {
                Harpymodloader.setRoleMaximum(ModRoles.RECORDER, 1);
            } else {
                Harpymodloader.setRoleMaximum(ModRoles.RECORDER, 0);
            }
            // 秃鹫数量
            if (players_count >= 8) {
                Harpymodloader.setRoleMaximum(ModRoles.VULTURE, 1);
            } else {
                Harpymodloader.setRoleMaximum(ModRoles.VULTURE, 0);
            }
            // 纵火犯数量
            if (players_count >= 12) {
                Harpymodloader.setRoleMaximum(SERoles.ARSONIST, 1);
            } else {
                Harpymodloader.setRoleMaximum(SERoles.ARSONIST, 0);
            }

            // 钟表匠数量 - 仅在12人以上对局出现
            if (players_count >= 12) {
                Harpymodloader.setRoleMaximum(ModRoles.CLOCKMAKER_ID, 1);
            } else {
                Harpymodloader.setRoleMaximum(ModRoles.CLOCKMAKER_ID, 0);
            }

            // 仇杀客数量 - 仅在12人以上对局出现
            if (players_count >= 12) {
                Harpymodloader.setRoleMaximum(ModRoles.BLOOD_FEUDIST_ID, 1);
            } else {
                Harpymodloader.setRoleMaximum(ModRoles.BLOOD_FEUDIST_ID, 0);
            }

            // 特殊警卫数量
            {
                int allSpecialPoliceCount = 0;

                if (players_count >= 48) {
                    allSpecialPoliceCount = 4;
                } else if (players_count >= 32) {
                    allSpecialPoliceCount = 3;
                } else if (players_count >= 18) {
                    allSpecialPoliceCount = 2;
                } else if (players_count >= 12) {
                    allSpecialPoliceCount = 1;
                } else {
                    allSpecialPoliceCount = 0;
                }
                if (allSpecialPoliceCount > 0) {
                    int PATROLLER_COUNT = 1;
                    if (allSpecialPoliceCount >= 2)
                        PATROLLER_COUNT = random.nextInt(1, allSpecialPoliceCount + 1);
                    else if (allSpecialPoliceCount >= 1) {
                        PATROLLER_COUNT = 1;
                    }
                    if (PATROLLER_COUNT > allSpecialPoliceCount) {
                        PATROLLER_COUNT = allSpecialPoliceCount;
                    }
                    int ELF_COUNT = allSpecialPoliceCount - PATROLLER_COUNT;
                    if (ELF_COUNT < 0)
                        ELF_COUNT = 0;
                    Harpymodloader.setRoleMaximum(ModRoles.PATROLLER, PATROLLER_COUNT);
                    Harpymodloader.setRoleMaximum(ModRoles.ELF, ELF_COUNT);

                    // BEST_VIGILANTE (更好的义警) - 0.1%概率生成
                    if (random.nextInt(0, 10000) < 10) {
                        Harpymodloader.setRoleMaximum(ModRoles.BEST_VIGILANTE_ID, 1);
                    } else {
                        Harpymodloader.setRoleMaximum(ModRoles.BEST_VIGILANTE_ID, 0);
                    }
                } else {
                    Harpymodloader.setRoleMaximum(ModRoles.PATROLLER, 0);
                    Harpymodloader.setRoleMaximum(ModRoles.ELF, 0);
                    Harpymodloader.setRoleMaximum(ModRoles.BEST_VIGILANTE_ID, 0);
                }

            }
            initModifiersCount(players_count);
        });
    }

    public static void initModifiersCount(int players) {
        Random random = new Random();
        // LOVERS
        REFUGEE_CHANCE = NoellesRolesConfig.HANDLER.instance().chanceOfModifierRefugee;
        if (REFUGEE_CHANCE < 0) {
            REFUGEE_CHANCE = 0;
        }
        //
        if (players >= 12 && random.nextInt(0, 100) <= 10) {
            StupidExpress.LOGGER.info("Modifier [Lovers] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("lovers"), 1);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("lovers"), 0);
        }

        /// REFUGEE
        if (players >= 12 && random.nextInt(0, 100) <= REFUGEE_CHANCE) {
            StupidExpress.LOGGER.info("Modifier [Refugee] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("refugee"), 1);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("refugee"), 0);
        }

        /// TINY
        StupidExpress.LOGGER.info("Modifier [Tiny] enabled in this round!");
        Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("tiny"), players / random.nextInt(4, 12));

        /// TALL
        StupidExpress.LOGGER.info("Modifier [Tall] enabled in this round!");
        Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("tall"), players / random.nextInt(4, 12));

        /// FEATHER
        StupidExpress.LOGGER.info("Modifier [Feather] enabled in this round!");
        Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("feather"), 2);

        /// MAGNATE
        if (random.nextInt(0, 100) < 50) {
            StupidExpress.LOGGER.info("Modifier [Magnate] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("magnate"), 2);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("magnate"), 0);
        }

        /// TASKMASTER
        if (random.nextInt(0, 100) < 30) {
            StupidExpress.LOGGER.info("Modifier [Taskmaster] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("taskmaster"), players / random.nextInt(8, 12));
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("taskmaster"), 0);
        }

        /// ALLERGIST
        if (random.nextInt(0, 100) < 20) {
            StupidExpress.LOGGER.info("Modifier [Allergist] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("allergist"), 1);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("allergist"), 0);
        }

        /// CURSED
        if (players >= 12 && random.nextInt(0, 100) < 30) {
            StupidExpress.LOGGER.info("Modifier [Cursed] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("cursed"), 1);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("cursed"), 0);
        }

        /// SECRETIVE
        if (players >= 12 && random.nextInt(0, 100) < 20) {
            StupidExpress.LOGGER.info("Modifier [Secretive] enabled in this round! (2)");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("secretive"), players / random.nextInt(8, 12));
        } else {
            StupidExpress.LOGGER.info("Modifier [Secretive] enabled in this round! (1)");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("secretive"), 1);
        }

        /// KNIGHT
        if (players >= 12 && random.nextInt(0, 100) < 10) {
            StupidExpress.LOGGER.info("Modifier [Knight] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("knight"), 1);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("knight"), 0);
        }
        SPLIT_PERSONALITY_CHANCE = NoellesRolesConfig.HANDLER.instance().chanceOfModifierSplitPersonality;
        if (SPLIT_PERSONALITY_CHANCE < 0) {
            SPLIT_PERSONALITY_CHANCE = 0;
        }
        /// SPLIT_PERSONALITY
        if (players >= 12 && random.nextInt(0, 100) < SPLIT_PERSONALITY_CHANCE) {
            StupidExpress.LOGGER.info("Modifier [Split Personality] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("split_personality"), 1);
        } else {
            Harpymodloader.MODIFIER_MAX.put(StupidExpress.id("split_personality"), 0);
        }

        /// EXPEDITION (远征队)
        // 50%概率生成
        if (random.nextInt(0, 100) < 50) {
            StupidExpress.LOGGER.info("Modifier [Expedition] enabled in this round!");
            Harpymodloader.MODIFIER_MAX.put(Noellesroles.id("expedition"), 1);
        } else {
            Harpymodloader.MODIFIER_MAX.put(Noellesroles.id("expedition"), 0);
        }
    }
}
