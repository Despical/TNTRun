package me.despical.tntrun.commands.admin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.commands.exception.CommandException;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ListCommand extends SubCommand {

	public ListCommand(String name) {
		super("list");
		setPermission("tntrun.admin.list");
	}

	@Override
	public String getPossibleArguments() {
		return "";
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) throws CommandException {
		if (ArenaRegistry.getArenas().size() == 0) {
			sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Admin-Commands.List-Command.No-Arenas-Created"));
			return;
		}
		List<String> arenas = ArenaRegistry.getArenas().stream().map(Arena::getId).collect(Collectors.toList());
		sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Admin-Commands.List-Command.Format").replace("%list%",
			arenas.toString().substring(1, arenas.toString().length() - 1)));
	}

	@Override
	public List<String> getTutorial() {
		return Arrays.asList("Show all of the existing arenas");
	}

	@Override
	public CommandType getType() {
		return CommandType.GENERIC;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.BOTH;
	}
}