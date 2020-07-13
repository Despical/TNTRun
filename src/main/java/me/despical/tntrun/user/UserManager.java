package me.despical.tntrun.user;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.user.data.FileStats;
import me.despical.tntrun.user.data.MysqlManager;
import me.despical.tntrun.user.data.UserDatabase;
import me.despical.tntrun.utils.Debugger;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class UserManager {

	private UserDatabase database;
	private List<User> users = new ArrayList<>();

	public UserManager(Main plugin) {
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			database = new MysqlManager(plugin);
			Debugger.debug(Level.INFO, "MySQL Stats enabled");
		} else {
			database = new FileStats(plugin);
			Debugger.debug(Level.INFO, "File Stats enabled");
		}
		loadStatsForPlayersOnline();
	}

	private void loadStatsForPlayersOnline() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			User user = getUser(player);
			loadStatistics(user);
		}
	}

	public User getUser(Player player) {
		for (User user : users) {
			if (user.getPlayer().equals(player)) {
				return user;
			}
		}
		Debugger.debug(Level.INFO, "Registering new user {0} ({1})", player.getUniqueId(), player.getName());
		User user = new User(player);
		users.add(user);
		return user;
	}
	
	public List<User> getUsers(Arena arena) {
		List<User> users = new ArrayList<>();
		for (Player player : arena.getPlayers()) {
			users.add(getUser(player));
		}
		return users;
	}

	public void saveStatistic(User user, StatsStorage.StatisticType stat) {
		if (!stat.isPersistent()) {
			return;
		}
		database.saveStatistic(user, stat);
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