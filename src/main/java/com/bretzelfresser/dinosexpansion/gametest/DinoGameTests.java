package com.bretzelfresser.dinosexpansion.gametest;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModEntities;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.TeamManagerImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.fml.ModList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

@GameTestHolder(DinosExpansion.MODID)
public class DinoGameTests {

    private static void initFtbPlayer(Player player) {
            var manager = FTBTeamsAPI.api().getManager();
            if (manager instanceof TeamManagerImpl impl) {
                impl.playerLoggedIn(null, player.getUUID(), player.getScoreboardName());
            }
    }

    @GameTest(template = "empty", batch = "ownership tests")
    public static void testTamedDinoAccessNoTeam(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        BaseDinoEntity dino = helper.spawn(ModEntities.CERATOSAURS.get(), pos);

        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Player other = helper.makeMockPlayer(GameType.SURVIVAL);

        // 1. Tame the dino
        dino.setTamedBy(owner);

        // 2. Owner should have access
        helper.assertTrue(dino.canPlayerAccess(owner, true), "Owner must have access to their tamed dinosaur.");

        // 3. Other player without team should NOT have access
        helper.assertFalse(dino.canPlayerAccess(other, true), "Non-owner without team must not have access.");

        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ownership tests")
    public static void testTamedDinoAccessScoreboardTeam(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);

        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Player other = helper.makeMockPlayer(GameType.SURVIVAL);

        helper.getLevel().addFreshEntity(owner);
        helper.getLevel().addFreshEntity(other);

        // 1. Tame the dino
        BaseDinoEntity dino = helper.spawn(ModEntities.CERATOSAURS.get(), pos);
        dino.setTamedBy(owner);

        // 2. Create scoreboard team and add both players
        Scoreboard scoreboard = helper.getLevel().getScoreboard();
        PlayerTeam team = scoreboard.addPlayerTeam("test_tamed_team");
        scoreboard.addPlayerToTeam(owner.getScoreboardName(), team);
        scoreboard.addPlayerToTeam(other.getScoreboardName(), team);

        try {
            // 3. Team member should have access
            helper.assertTrue(dino.canPlayerAccess(other, true), "Vanilla scoreboard team member must have access to the tamed dinosaur.");
        } finally {
            // Clean up scoreboard team
            scoreboard.removePlayerTeam(team);
        }

        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ownership tests")
    public static void testTamedDinoAccessFTBTeam(GameTestHelper helper){
        // Skip if FTB Teams is not present in runtime environment
        if (!ModList.get().isLoaded("ftbteams")) {
            helper.succeed();
            return;
        }

        BlockPos pos = new BlockPos(2, 2, 2);
        BaseDinoEntity dino = helper.spawn(ModEntities.CERATOSAURS.get(), pos);

        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Player other = helper.makeMockPlayer(GameType.SURVIVAL);

        helper.getLevel().addFreshEntity(owner);
        helper.getLevel().addFreshEntity(other);

        // 0. Initialize FTB Teams data for mock players
        initFtbPlayer(owner);
        initFtbPlayer(other);

        // 1. Tame the dino
        dino.setTamedBy(owner);
        //this is really hacky, not recommended inside the read code, but for the tests thats fine
        if (FTBTeamsAPI.api().getManager() instanceof TeamManagerImpl teamManager){
            try {
                var partyTeam = teamManager.createParty(owner.getUUID(), null, "tame_party_test", "nothing to do here", null);
                partyTeam.join(null, other.getGameProfile());

                helper.assertTrue(dino.canPlayerAccess(other, true), "FTB Teams team member must have access to the tamed dinosaur.");




            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ownership tests")
    public static void testUnconsciousDinoAccessNoTeam(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        BaseDinoEntity dino = helper.spawn(ModEntities.CERATOSAURS.get(), pos);

        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Player other = helper.makeMockPlayer(GameType.SURVIVAL);

        helper.getLevel().addFreshEntity(owner);
        helper.getLevel().addFreshEntity(other);

        // 1. Set unconscious owner
        dino.setUnconsciousFrom(owner);

        // 2. Unconscious owner should have access
        helper.assertTrue(dino.canPlayerAccess(owner, true), "Unconscious owner must have access.");

        // 3. Other player without team should NOT have access
        helper.assertFalse(dino.canPlayerAccess(other, true), "Non-owner without team must not have access to unconscious dinosaur.");

        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ownership tests")
    public static void testUnconsciousDinoAccessScoreboardTeam(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        BaseDinoEntity dino = helper.spawn(ModEntities.CERATOSAURS.get(), pos);

        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Player other = helper.makeMockPlayer(GameType.SURVIVAL);

        helper.getLevel().addFreshEntity(owner);
        helper.getLevel().addFreshEntity(other);

        // 1. Set unconscious owner
        dino.setUnconsciousFrom(owner);

        // 2. Create scoreboard team and add both players
        Scoreboard scoreboard = helper.getLevel().getScoreboard();
        PlayerTeam team = scoreboard.addPlayerTeam("test_unconscious_team");
        scoreboard.addPlayerToTeam(owner.getScoreboardName(), team);
        scoreboard.addPlayerToTeam(other.getScoreboardName(), team);

        try {
            // 3. Team member should have access
            helper.assertTrue(dino.canPlayerAccess(other, true), "Vanilla scoreboard team member must have access to the unconscious dinosaur.");
        } finally {
            // Clean up scoreboard team
            scoreboard.removePlayerTeam(team);
        }

        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ownership tests")
    public static void testUnconsciousDinoAccessFTBTeam(GameTestHelper helper) {
        // Skip if FTB Teams is not present in runtime environment
        if (!ModList.get().isLoaded("ftbteams")) {
            helper.succeed();
            return;
        }

        BlockPos pos = new BlockPos(2, 2, 2);
        BaseDinoEntity dino = helper.spawn(ModEntities.CERATOSAURS.get(), pos);

        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Player other = helper.makeMockPlayer(GameType.SURVIVAL);

        helper.getLevel().addFreshEntity(owner);
        helper.getLevel().addFreshEntity(other);

        // 0. Initialize FTB Teams data for mock players
        initFtbPlayer(owner);
        initFtbPlayer(other);

        // 1. Set unconscious owner
        dino.setUnconsciousFrom(owner);

        if (FTBTeamsAPI.api().getManager() instanceof TeamManagerImpl teamManager){
            try {
                var partyTeam = teamManager.createParty(owner.getUUID(), null, "unconscious_party_test", "nothing to do here", null);
                partyTeam.join(null, other.getGameProfile());

                helper.assertTrue(dino.canPlayerAccess(other, true), "FTB Teams team member must have access to the tamed dinosaur.");




            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        helper.succeed();
    }

    @AfterBatch(batch = "ownership tests")
    public static void cleanParties(ServerLevel serverLevel){
        Path directory = serverLevel.getServer().getWorldPath(TeamManagerImpl.FOLDER_NAME);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return;
        }

        try (Stream<Path> stream = Files.walk(directory)) {
            stream
                    // Reverse order so files and subfolders are deleted BEFORE parent folders
                    .sorted(Comparator.reverseOrder())
                    // Don't delete the root folder itself
                    .filter(path -> !path.equals(directory))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete " + path, e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
