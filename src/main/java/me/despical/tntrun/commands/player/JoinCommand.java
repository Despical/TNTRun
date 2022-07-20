package me.despical.tntrun.commands.player;

import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public class JoinCommand extends SubCommand {

	public JoinCommand() {
		super ("join");
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
		final Player player = (Player) sender;

		if (args.length == 0) {
			player.sendMessage(chatManager.prefixedMessage("commands.type_arena_name"));
			return;
		}

		final Arena arena = ArenaRegistry.getArena(args[0]);

		if (arena != null) {
			ArenaManager.joinAttempt(player, arena);
			return;
		}

		player.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
	}

	@Override
	public String getTutorial() {
		return null;
	}

	@Override
	public int getType() {
		return HIDDEN;
	}

	@Override
	public int getSenderType() {
		return PLAYER;
	}
}