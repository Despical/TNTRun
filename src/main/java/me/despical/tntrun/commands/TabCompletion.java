package me.despical.tntrun.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class TabCompletion implements TabCompleter {

	public List<String> commands = new ArrayList<>();

	public TabCompletion(CommandHandler commandHandler) {
		for (SubCommand command : commandHandler.getSubCommands()) {
			this.commands.add(command.getName().toLowerCase(Locale.ENGLISH));
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = new ArrayList<>();

		if (!(sender instanceof Player)) {
			return Collections.emptyList();
		}

		Player player = (Player) sender;

		if (!(player.hasPermission("oitc.admin"))) {
			return Collections.emptyList();
		}

		if (args.length == 1) {
			StringUtil.copyPartialMatches(args[0], commands, completions);
		}

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("list") ||
				args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("randomjoin") || args[0].equalsIgnoreCase("stop") ||
				args[0].equalsIgnoreCase("forcestart")) {
				return Collections.emptyList();
			}

			if (args[0].equalsIgnoreCase("top")) {
				return Arrays.asList("games_played", "wins", "loses", "longest_survive", "coins");
			}

			if (args[0].equalsIgnoreCase("stats")) {
				return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
			}

			List<String> arenas = ArenaRegistry.getArenas().stream().map(Arena::getId).collect(Collectors.toList());
			StringUtil.copyPartialMatches(args[1], arenas, completions);
			Collections.sort(arenas);
			return arenas;
		}

		Collections.sort(completions);
		return completions;
	}
}