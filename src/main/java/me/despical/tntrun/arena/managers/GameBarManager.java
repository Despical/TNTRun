package me.despical.tntrun.arena.managers;

import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.user.User;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.jetbrains.annotations.Nullable;

/**
 * @author Despical
 * <p>
 * Created at 21.05.2023
 */
public class GameBarManager {

	@Nullable
	private BossBar gameBar;

	private final Arena arena;
	private final Main plugin;
	private final boolean enabled;

	public GameBarManager(final Arena arena, final Main plugin) {
		this.arena = arena;
		this.plugin = plugin;
		this.enabled = plugin.getOption(ConfigPreferences.Option.GAME_BAR_ENABLED);

		if (enabled) {
			this.gameBar = plugin.getServer().createBossBar("", BarColor.BLUE, BarStyle.SOLID);
		}
	}

	public void doBarAction(final User user, int action) {
		if (!enabled) return;

		final var player = user.getPlayer();

		if (action == 1) {
			this.gameBar.addPlayer(player);
		} else {
			this.gameBar.removePlayer(player);
		}
	}

	public void removeAll() {
		if (this.gameBar != null) this.gameBar.removeAll();
	}

	public void handleGameBar() {
		if (this.gameBar != null)

			switch (arena.getArenaState()) {
				case WAITING_FOR_PLAYERS -> setTitle("game-bar.waiting-for-players");
				case STARTING -> setTitle("game-bar.starting");
				case IN_GAME -> setTitle("game-bar.in-game");
				case ENDING -> setTitle("game-bar.ending");
			}

		gameBar.setVisible(!this.gameBar.getTitle().isEmpty());
	}

	private void setTitle(final String path) {
		this.gameBar.setTitle(plugin.getChatManager().message(path));
	}
}