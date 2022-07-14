package me.despical.tntrun.commands.player;

import me.despical.commons.util.Collections;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public class RandomJoinCommand extends SubCommand {

	public RandomJoinCommand() {
		super ("randomjoin");
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
		List<Arena> arenas = ArenaRegistry.getArenas().stream().filter(arena -> Collections.contains(arena.getArenaState(), ArenaState.WAITING_FOR_PLAYERS, ArenaState.STARTING)
			&& arena.getPlayers().size() < arena.getMaximumPlayers()).collect(Collectors.toList());

		if (!arenas.isEmpty()) {
			ArenaManager.joinAttempt((Player) sender, arenas.get(0));
			return;
		}

		sender.sendMessage(chatManager.prefixedMessage("commands.no_free_arenas"));
	}

	@Override
	public String getTutorial() {
		return null;
	}

	@Override
	public CommandType getType() {
		return CommandType.HIDDEN;
	}

	@Override
	public SubCommand.SenderType getSenderType() {
		return SubCommand.SenderType.PLAYER;
	}
}