package me.despical.tntrun.commands;

import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.Completer;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import org.bukkit.util.StringUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 21.05.2023
 */
public class TabCompleter extends AbstractCommand {

	public TabCompleter(final Main plugin) {
		super(plugin);
	}

	@Completer(
		name = "tntrun",
		aliases = {"tr"}
	)
	public List<String> onTabComplete(CommandArguments arguments) {
		final List<String> completions = new ArrayList<>(), commands = plugin.getCommandFramework().getCommands().stream().map(cmd -> cmd.name().replace(arguments.getLabel() + '.', "")).collect(Collectors.toList());
		final String args[] = arguments.getArguments(), arg = args[0];

		commands.remove("tntrun");

		if (args.length == 1) {
			StringUtil.copyPartialMatches(arg, arguments.hasPermission("tntrun.admin") || arguments.getSender().isOp() ? commands : List.of("top", "stats", "join", "leave", "randomjoin"), completions);
		}

		if (args.length == 2) {
			if (List.of("create", "list", "randomjoin", "leave").contains(arg)) return null;

			if (arg.equalsIgnoreCase("top")) {
				return List.of("wins", "loses", "highest_score", "games_played");
			}

			if (arg.equalsIgnoreCase("stats")) {
				return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
			}

			final List<String> arenas = plugin.getArenaRegistry().getArenas().stream().map(Arena::getId).collect(Collectors.toList());

			StringUtil.copyPartialMatches(args[1], arenas, completions);
			arenas.sort(null);
			return arenas;
		}

		completions.sort(null);
		return completions;
	}
}