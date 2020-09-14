package me.despical.tntrun.commands.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.commands.SubCommand;

import static me.despical.tntrun.arena.ArenaManager.*;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class RandomJoinCommand extends SubCommand {

	public RandomJoinCommand() {
		super("randomjoin");
	}

	@Override
	public String getPossibleArguments() {
		return null;
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		if (getPlugin().getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
			return;
		}

		Map<Arena, Integer> arenas = new HashMap<>();

		for (Arena arena : ArenaRegistry.getArenas()) {
			if (arena.getArenaState() == ArenaState.STARTING && arena.getPlayers().size() < arena.getMaximumPlayers()) {
				arenas.put(arena, arena.getPlayers().size());
			}
		}

		if (arenas.size() > 0) {
			Stream<Map.Entry<Arena, Integer>> sorted = arenas.entrySet().stream().sorted(Map.Entry.comparingByValue());
			Arena arena = sorted.findFirst().get().getKey();
			if (arena != null) {
				joinAttempt((Player) sender, arena);
				return;
			}
		}

		for (Arena arena : ArenaRegistry.getArenas()) {
			if ((arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS || arena.getArenaState() == ArenaState.STARTING) && arena.getPlayers().size() < arena.getMaximumPlayers()) {
				joinAttempt((Player) sender, arena);
				return;
			}
		}

		sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.No-Free-Arenas"));
	}

	@Override
	public List<String> getTutorial() {
		return null;
	}

	@Override
	public CommandType getType() {
		return CommandType.HIDDEN;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.PLAYER;
	}
}