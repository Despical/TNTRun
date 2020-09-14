package me.despical.tntrun.arena.managers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.scoreboard.ScoreboardLib;
import me.despical.commonsbox.scoreboard.common.EntryBuilder;
import me.despical.commonsbox.scoreboard.type.Entry;
import me.despical.commonsbox.scoreboard.type.Scoreboard;
import me.despical.commonsbox.scoreboard.type.ScoreboardHandler;
import me.despical.commonsbox.string.StringFormatUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.user.User;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2029
 */
public class ScoreboardManager {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static final String boardTitle = plugin.getChatManager().colorMessage("Scoreboard.Title");
	private final List<Scoreboard> scoreboards = new ArrayList<>();
	private final Arena arena;
	private final FileConfiguration config = ConfigUtils.getConfig(plugin, "messages");
	
	public ScoreboardManager(Arena arena) {
		this.arena = arena;
	}

	/**
	 * Creates arena scoreboard for target user
	 *
	 * @param user user that represents game player
	 * @see User
	 */
	public void createScoreboard(User user) {
		Scoreboard scoreboard = ScoreboardLib.createScoreboard(user.getPlayer()).setHandler(new ScoreboardHandler() {

			@Override
			public String getTitle(Player player) {
				return boardTitle;
			}

			@Override
			public List<Entry> getEntries(Player player) {
				return formatScoreboard(user);
			}
		});

		scoreboard.activate();
		scoreboards.add(scoreboard);
	}

	/**
	 * Removes scoreboard of user
	 *
	 * @param user user that represents game player
	 * @see User
	 */
	public void removeScoreboard(User user) {
		for (Scoreboard board : scoreboards) {
			if (board.getHolder().equals(user.getPlayer())) {
				scoreboards.remove(board);
				board.deactivate();
				return;
			}
		}
	}

	/**
	 * Forces all scoreboards to deactivate.
	 */
	public void stopAllScoreboards() {
		for (Scoreboard board : scoreboards) {
			board.deactivate();
		}

		scoreboards.clear();
	}

	private List<Entry> formatScoreboard(User user) {
		EntryBuilder builder = new EntryBuilder();
		List<String> lines;

		if (arena.getArenaState() == ArenaState.IN_GAME) {
			lines = config.getStringList("Scoreboard.Content.Playing");
		} else {
			if (arena.getArenaState() == ArenaState.ENDING) {
				lines = config.getStringList("Scoreboard.Content.Playing");
			} else {
				lines = config.getStringList("Scoreboard.Content." + arena.getArenaState().getFormattedName());
			}
		}

		for (String line : lines) {
				builder.next(formatScoreboardLine(line, user));
		}

		return builder.build();
	}

	private String formatScoreboardLine(String line, User user) {
		String formattedLine = line;
		formattedLine = StringUtils.replace(formattedLine, "%time%", String.valueOf(arena.getTimer()));
		formattedLine = StringUtils.replace(formattedLine, "%formatted_time%",StringFormatUtils.formatIntoMMSS(arena.getTimer()));
		formattedLine = StringUtils.replace(formattedLine, "%mapname%", arena.getMapName());
		formattedLine = StringUtils.replace(formattedLine, "%players%", String.valueOf(arena.getPlayers().size()));
		formattedLine = StringUtils.replace(formattedLine, "%max_players%", String.valueOf(arena.getMaximumPlayers()));
		formattedLine = StringUtils.replace(formattedLine, "%min_players%", String.valueOf(arena.getMinimumPlayers()));
		formattedLine = StringUtils.replace(formattedLine, "%coins_earned%", String.valueOf(StatsStorage.getUserStats(user.getPlayer(), StatsStorage.StatisticType.LOCAL_COINS)));
		formattedLine = StringUtils.replace(formattedLine, "%double_jumps%", String.valueOf(StatsStorage.getUserStats(user.getPlayer(), StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS)));
		formattedLine = plugin.getChatManager().colorRawMessage(formattedLine);

		if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			formattedLine = PlaceholderAPI.setPlaceholders(user.getPlayer(), formattedLine);
		}

		return formattedLine;
	}
}