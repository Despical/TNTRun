package me.despical.tntrun.commands.game;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.commands.exception.CommandException;

/**
 * @author Despical
 * <p>
 * Created at 02.07.2020
 */
public class JoinCommand extends SubCommand {

	public JoinCommand(String name) {
		super("join");
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
	public void execute(CommandSender sender, String label, String[] args) throws CommandException {
		if (args.length == 0) {
			sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Type-Arena-Name"));
			return;
		}
		for (Arena arena : ArenaRegistry.getArenas()) {
			if (args[0].equalsIgnoreCase(arena.getId())) {
				ArenaManager.joinAttempt((Player) sender, arena);
				return;
			}
		}
		sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.No-Arena-Like-That"));
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