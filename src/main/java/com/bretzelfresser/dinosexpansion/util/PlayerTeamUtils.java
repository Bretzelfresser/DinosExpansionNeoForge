package com.bretzelfresser.dinosexpansion.util;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class PlayerTeamUtils {

    public static boolean arePlayersInSameTeam(Level level, UUID uuid1, UUID uuid2) {
        if (uuid1.equals(uuid2)) {
            return true;
        }

        // 1. Try FTB Teams Integration
        if (ModList.get().isLoaded("ftbteams")) {
            if (arePlayersInSameFTBTeam(uuid1, uuid2)) {
                return true;
            }
        }

        // 2. Try Vanilla Scoreboard Teams
        return arePlayersInSameVanillaTeam(level, uuid1, uuid2);
    }

    private static boolean arePlayersInSameFTBTeam(UUID uuid1, UUID uuid2) {
        try {
            Optional<Team> team1Opt = FTBTeamsAPI.api().getManager().getTeamForPlayerID(uuid1);
            Optional<Team> team2Opt = FTBTeamsAPI.api().getManager().getTeamForPlayerID(uuid2);
            if (team1Opt.isPresent() && team2Opt.isPresent()) {
                Team team1 = team1Opt.get();
                Team team2 = team2Opt.get();
                return team1.getId() != null && team1.getId().equals(team2.getId());
            }
        } catch (Throwable t) {
            // Fallback in case of class loading / version differences at runtime
        }
        return false;
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
