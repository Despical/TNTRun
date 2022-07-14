package me.despical.tntrun.commands.admin;

import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public class StopCommand extends SubCommand {

	public StopCommand() {
		super ("stop");

		setPermission("tntrun.admin.stop");
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
		Player player = (Player) sender;
		Arena arena = ArenaRegistry.getArena(player);

		if (arena == null) {
			player.sendMessage(chatManager.prefixedMessage("commands.not_playing", player));
			return;
		}

		if (arena.getArenaState() != ArenaState.ENDING) {
			ArenaManager.stopGame(true, arena);
		}
	}

	@Override
	public String getTutorial() {
		return "Force start arena that user in";
	}

	@Override
	public CommandType getType() {
		return CommandType.GENERIC;
	}

	@Override
	public SenderType getSenderType() {
		return SubCommand.SenderType.PLAYER;
	}
}