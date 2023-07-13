package me.despical.tntrun.utils;

import de.rapha149.signgui.version.*;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.user.User;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2023
 */
public class Utils {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	private Utils() {
	}

	public static void applyActionBarCooldown(final User user, int seconds, final Consumer<User> consumer) {
		new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				final var arena = user.getArena();

				if (arena == null || arena.getArenaState() != ArenaState.IN_GAME) cancel();
				if (arena.isDeathPlayer(user)) cancel();
				if (ticks >= seconds * 20) {
					if (consumer != null) consumer.accept(user);
					cancel();
				}

				var progress = getProgressBar(ticks, seconds * 20);
				user.sendActionBar(plugin.getChatManager().message("messages.in-game.cooldown-format", user).replace("%progress%", progress).replace("%time%", Double.toString((double) ((seconds * 20) - ticks) / 20)));

				ticks += 2;
			}
		}.runTaskTimer(plugin, 0, 2);
	}

	public static void applyActionBarCooldown(final User user, int seconds) {
		applyActionBarCooldown(user, seconds, null);
	}

	private static String getProgressBar(int current, int max) {
		float percent = (float) current / max;
		int progressBars = (int) (10 * percent), leftOver = (10 - progressBars);

		return "§a" +
			"■".repeat(Math.max(0, progressBars)) +
			"§c" +
			"■".repeat(Math.max(0, leftOver));
	}

	public @SuppressWarnings("unused") static void addItem(Player player, ItemStack itemStack, int itemPos) {
		if (player == null) return;

		var inv = player.getInventory();
		var item = inv.getItem(itemPos);

		if (item != null) {
			item.setAmount(item.getAmount() + itemStack.getAmount());
			return;
		}

		inv.setItem(itemPos, itemStack);
	}

	static {
		@SuppressWarnings("unused") final Class<?>[] shadePluginHack = {
			Wrapper1_20_R1.class,
			Wrapper1_19_R3.class,
			Wrapper1_19_R2.class,
			Wrapper1_19_R1.class,
			Wrapper1_18_R2.class,
			Wrapper1_17_R1.class,
			Wrapper1_16_R3.class,
			Wrapper1_15_R1.class,
			Wrapper1_14_R1.class,
			Wrapper1_13_R2.class,
			Wrapper1_12_R1.class,
			Wrapper1_11_R1.class,
			Wrapper1_10_R1.class,
			Wrapper1_9_R2.class,
		};
	}
}