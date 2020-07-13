package me.despical.tntrun.commands.admin.arena;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.commands.exception.CommandException;
import me.despical.tntrun.handlers.setup.SetupInventory;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class EditCommand extends SubCommand {

	public EditCommand(String name) {
		super("edit");
		setPermission("tntrun.admin.setup");
	}

	@Override
	public String getPossibleArguments() {
		return "<arena>";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) throws CommandException {
		if (ArenaRegistry.getArena(args[0]) == null) {
			sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.No-Arena-Like-That"));
			return;
		}
		new SetupInventory(ArenaRegistry.getArena(args[0]), (Player) sender).openInventory();	
	}

	@Override
	public List<String> getTutorial() {
		return Arrays.asList("Open arena editor menu");
	}

	@Override
	public CommandType getType() {
		return CommandType.GENERIC;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.PLAYER;
	}
}