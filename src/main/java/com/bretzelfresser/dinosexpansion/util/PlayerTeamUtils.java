package com.bretzelfresser.dinosexpansion.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public class PlayerTeamUtils {

    public static boolean arePlayersInSameTeam(Level level, UUID uuid1, UUID uuid2) {
        if (uuid1.equals(uuid2)) {
            return true;
        }

        // 1. Try FTB Teams Integration reflectively
        if (arePlayersInSameFTBTeam(uuid1, uuid2)) {
            return true;
        }

        // 2. Try Vanilla Scoreboard Teams
        return arePlayersInSameVanillaTeam(level, uuid1, uuid2);
    }

    private static boolean arePlayersInSameFTBTeam(UUID uuid1, UUID uuid2) {
        try {
            Class<?> apiClass = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
            Object apiInstance = apiClass.getMethod("api").invoke(null);
            if (apiInstance == null) return false;

            Object manager = apiInstance.getClass().getMethod("getManager").invoke(apiInstance);
            if (manager == null) return false;

            Method getTeamMethod = getGetTeamMethod(manager);
            if (getTeamMethod == null) return false;

            Object team1Opt = getTeamMethod.invoke(manager, uuid1);
            Object team2Opt = getTeamMethod.invoke(manager, uuid2);
            if (team1Opt == null || team2Opt == null) return false;

            Object team1 = team1Opt instanceof Optional ? ((Optional<?>) team1Opt).orElse(null) : team1Opt;
            Object team2 = team2Opt instanceof Optional ? ((Optional<?>) team2Opt).orElse(null) : team2Opt;
            if (team1 == null || team2 == null) return false;

            java.lang.reflect.Method getIdMethod = team1.getClass().getMethod("getId");
            UUID id1 = (UUID) getIdMethod.invoke(team1);
            UUID id2 = (UUID) getIdMethod.invoke(team2);

            return id1 != null && id1.equals(id2);
        } catch (Exception e) {
            return false;
        }
    }

    private static @Nullable Method getGetTeamMethod(Object manager) {
        Method getTeamMethod = null;
        try {
            getTeamMethod = manager.getClass().getMethod("getTeamForPlayerID", UUID.class);
        } catch (NoSuchMethodException e) {
            try {
                getTeamMethod = manager.getClass().getMethod("getTeamForPlayer", UUID.class);
            } catch (NoSuchMethodException ex) {
                for (Method m : manager.getClass().getMethods()) {
                    if ((m.getName().equals("getTeamForPlayer") || m.getName().equals("getTeamForPlayerID"))
                            && m.getParameterCount() == 1
                            && m.getParameterTypes()[0] == UUID.class) {
                        getTeamMethod = m;
                        break;
                    }
                }
            }
        }
        return getTeamMethod;
    }

    private static boolean arePlayersInSameVanillaTeam(Level level, UUID uuid1, UUID uuid2) {
        Scoreboard scoreboard = level.getScoreboard();

        String name1 = getPlayerNameForTeam(level, uuid1);
        String name2 = getPlayerNameForTeam(level, uuid2);
        if (name1 == null || name2 == null) {
            return false;
        }

        PlayerTeam team1 = scoreboard.getPlayersTeam(name1);
        PlayerTeam team2 = scoreboard.getPlayersTeam(name2);

        return team1 != null && team2 != null && team1.isAlliedTo(team2);
    }

    private static String getPlayerNameForTeam(Level level, UUID uuid) {
        Player player = level.getPlayerByUUID(uuid);
        if (player != null) {
            return player.getScoreboardName();
        }
        if (level.getServer() != null) {
            var profile = level.getServer().getProfileCache().get(uuid).orElse(null);
            if (profile != null) {
                return profile.getName();
            }
        }
        return null;
    }
}
