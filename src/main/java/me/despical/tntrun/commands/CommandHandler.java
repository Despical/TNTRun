package me.despical.tntrun.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.despical.commonsbox.string.StringMatcher;
import me.despical.tntrun.Main;
import me.despical.tntrun.commands.SubCommand.SenderType;
import me.despical.tntrun.commands.admin.HelpCommand;
import me.despical.tntrun.commands.admin.ListCommand;
import me.despical.tntrun.commands.admin.arena.DeleteCommand;
import me.despical.tntrun.commands.admin.arena.EditCommand;
import me.despical.tntrun.commands.admin.arena.ForceStartCommand;
import me.despical.tntrun.commands.admin.arena.ReloadCommand;
import me.despical.tntrun.commands.admin.arena.StopCommand;
import me.despical.tntrun.commands.exception.CommandException;
import me.despical.tntrun.commands.game.CreateCommand;
import me.despical.tntrun.commands.game.JoinCommand;
import me.despical.tntrun.commands.game.LeaderBoardCommand;
import me.despical.tntrun.commands.game.LeaveCommand;
import me.despical.tntrun.commands.game.RandomJoinCommand;
import me.despical.tntrun.commands.game.StatsCommand;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class CommandHandler implements CommandExecutor {

	private List<SubCommand> subCommands;
	private Map<Class<? extends SubCommand>, SubCommand> subCommandsByClass;
	private Main plugin;
	private TabCompletion tabCompletion;
	
	public CommandHandler(Main plugin) {
		this.plugin = plugin;
		subCommands = new ArrayList<>();
		subCommandsByClass = new HashMap<>();
		
		registerSubCommand(new CreateCommand("create"));
		registerSubCommand(new EditCommand("edit"));
		registerSubCommand(new DeleteCommand("delete"));
		registerSubCommand(new ReloadCommand("reload"));
		registerSubCommand(new ListCommand("list"));
		registerSubCommand(new HelpCommand("help"));
		registerSubCommand(new ForceStartCommand("forcestart"));
		registerSubCommand(new StopCommand("stop"));
		registerSubCommand(new JoinCommand("join"));
		registerSubCommand(new RandomJoinCommand("randomjoin"));
		registerSubCommand(new LeaveCommand("leave"));
		registerSubCommand(new StatsCommand("stats"));
		registerSubCommand(new LeaderBoardCommand("top"));
		
		tabCompletion = new TabCompletion(this);
		plugin.getCommand("tntrun").setExecutor(this);
		plugin.getCommand("tntrun").setTabCompleter(tabCompletion);
	}
	
	public void registerSubCommand(SubCommand subCommand) {
		subCommands.add(subCommand);
		subCommandsByClass.put(subCommand.getClass(), subCommand);
	}
	
	public List<SubCommand> getSubCommands() {
		return new ArrayList<>(subCommands);
	}
	
	public SubCommand getSubCommand(Class<? extends SubCommand> subCommandClass) {
		return subCommandsByClass.get(subCommandClass);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatColor.DARK_AQUA + "This server is running " + ChatColor.AQUA + "TNT Run " + ChatColor.DARK_AQUA + "v" + this.plugin.getDescription().getVersion() + " by " + ChatColor.AQUA + "Despical");
			if (sender.hasPermission("oitc.admin")) {
				sender.sendMessage(ChatColor.DARK_AQUA + "Commands: " + ChatColor.AQUA + "/" + label + " help");
			}
			return true;
		}
		for (SubCommand subCommand : subCommands) {
			if (subCommand.isValidTrigger(args[0])) {
				if (!subCommand.hasPermission(sender)) {
					sender.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("Commands.No-Permission"));
					return true;
				}
				if (subCommand.getSenderType() == SenderType.PLAYER && !(sender instanceof Player)) {
					sender.sendMessage(plugin.getChatManager().colorMessage("Commands.Only-By-Player"));
					return false;
				}
				if (args.length - 1 >= subCommand.getMinimumArguments()) {
					try {
						subCommand.execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
					} catch (CommandException e) {
						sender.sendMessage(ChatColor.RED + e.getMessage());
					}
				} else {
					if (subCommand.getType() == SubCommand.CommandType.GENERIC) {
						sender.sendMessage(ChatColor.RED + "Usage: /" + label + " " + subCommand.getName() + " " + (subCommand.getPossibleArguments().length() > 0 ? subCommand.getPossibleArguments() : ""));
					}
				}
				return true;
			}
		}
		List<StringMatcher.Match> matches = StringMatcher.match(args[0], subCommands.stream().map(SubCommand::getName).collect(Collectors.toList()));
        if (!matches.isEmpty()) {
          sender.sendMessage(plugin.getChatManager().colorMessage("Commands.Did-You-Mean").replace("%command%", label + " " + matches.get(0).getMatch()));
          return true;
        }
        return true;
	}
}