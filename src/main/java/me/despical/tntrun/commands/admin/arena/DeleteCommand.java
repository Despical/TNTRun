package me.despical.tntrun.commands.admin.arena;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.commands.exception.CommandException;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class DeleteCommand extends SubCommand {

	private Set<CommandSender> confirmations = new HashSet<>();
	
	public DeleteCommand(String name) {
		super("delete");
		setPermission("tntrun.admin.delete");
	}

	@Override
	public String getPossibleArguments() {
		return "<arena>";
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
		Arena arena = ArenaRegistry.getArena(args[0]);
		if (arena == null) {
			sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.No-Arena-Like-That"));
			return;
		}
		if (!confirmations.contains(sender)) {
			confirmations.add(sender);
			Bukkit.getScheduler().runTaskLater(getPlugin(), () -> confirmations.remove(sender), 20 * 10);
			sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Are-You-Sure"));
			return;
		}
		confirmations.remove(sender);
		ArenaManager.stopGame(true, arena);
		ArenaRegistry.unregisterArena(arena);
		FileConfiguration config = ConfigUtils.getConfig(getPlugin(), "arenas");
		config.set("instances." + args[0], null);
		ConfigUtils.saveConfig(getPlugin(), config, "arenas");
		sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Removed-Game-Instance"));
	}

	@Override
	public List<String> getTutorial() {
		return Arrays.asList("Delete specified arena");
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