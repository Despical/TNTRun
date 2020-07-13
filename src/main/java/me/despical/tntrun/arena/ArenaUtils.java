package me.despical.tntrun.arena;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ArenaUtils {

	private static Main plugin = JavaPlugin.getPlugin(Main.class);

	public static boolean areInSameArena(Player one, Player two) {
		if (ArenaRegistry.getArena(one) == null || ArenaRegistry.getArena(two) == null) {
			return false;
		}
		return ArenaRegistry.getArena(one).equals(ArenaRegistry.getArena(two));
	}
	
	public static void hidePlayer(Player p, Arena arena) {
		for (Player player : arena.getPlayers()) {
			player.hidePlayer(plugin, p);
		}
	}

	public static void showPlayer(Player p, Arena arena) {
		for (Player player : arena.getPlayers()) {
			player.showPlayer(plugin, p);
		}
	}

	public static void hidePlayersOutsideTheGame(Player player, Arena arena) {
		for (Player players : plugin.getServer().getOnlinePlayers()) {
			if (arena.getPlayers().contains(players)) {
				continue;
			}
			player.hidePlayer(plugin, players);
			players.hidePlayer(plugin, player);
		}
	}

	public static void updateNameTagsVisibility(final Player p) {
		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.NAMETAGS_HIDDEN)) {
			return;
		}
		for (Player players : plugin.getServer().getOnlinePlayers()) {
			Arena arena = ArenaRegistry.getArena(players);
			if (arena == null) {
				continue;
			}
			Scoreboard scoreboard = players.getScoreboard();
			if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
				scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
			}
			Team team = scoreboard.getTeam("TRHide");
			if (team == null) {
				team = scoreboard.registerNewTeam("TRHide");
			}
			team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
			if (arena.getArenaState() == ArenaState.IN_GAME) {
				team.addEntry(p.getName());
			} else if (arena.getArenaState() == ArenaState.STARTING || arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS) {
				team.removeEntry(p.getName());
			} else if (arena.getArenaState() == ArenaState.ENDING || arena.getArenaState() == ArenaState.RESTARTING) {
				team.removeEntry(p.getName());
			}
			players.setScoreboard(scoreboard);
		}
	}
}