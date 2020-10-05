package me.despical.tntrun.user;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.despical.tntrun.utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.user.data.FileStats;
import me.despical.tntrun.user.data.MysqlManager;
import me.despical.tntrun.user.data.UserDatabase;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class UserManager {

	private final UserDatabase database;
	private final List<User> users = new ArrayList<>();

	public UserManager(Main plugin) {
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			database = new MysqlManager(plugin);

			Debugger.debug("MySQL Stats enabled");
		} else {
			database = new FileStats(plugin);

			Debugger.debug("File Stats enabled");
		}

		loadStatsForPlayersOnline();
	}

	private void loadStatsForPlayersOnline() {
		Bukkit.getServer().getOnlinePlayers().stream().map(this::getUser).forEach(this::loadStatistics);
	}

	public User getUser(Player player) {
		for (User user : users) {
			if (user.getPlayer().equals(player)) {
				return user;
			}
		}

		Debugger.debug("Registering new user {0} ({1})", player.getUniqueId(), player.getName());

		User user = new User(player);

		users.add(user);
		return user;
	}
	
	public List<User> getUsers(Arena arena) {
		return arena.getPlayers().stream().map(this::getUser).collect(Collectors.toList());
	}

	public void saveStatistic(User user, StatsStorage.StatisticType stat) {
		if (!stat.isPersistent()) {
			return;
		}

		database.saveStatistic(user, stat);
	}

	public void saveAllStatistic(User user) {
		database.saveAllStatistic(user);
	}

	public void loadStatistics(User user) {
		database.loadStatistics(user);
	}

	public void removeUser(User user) {
		users.remove(user);
	}

	public UserDatabase getDatabase() {
		return database;
	}
}