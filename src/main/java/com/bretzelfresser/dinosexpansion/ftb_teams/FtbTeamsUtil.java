package com.bretzelfresser.dinosexpansion.ftb_teams;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;

import java.util.Optional;
import java.util.UUID;

public class FtbTeamsUtil {

    public static boolean arePlayersInSameFTBTeam(UUID uuid1, UUID uuid2) {
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
}
