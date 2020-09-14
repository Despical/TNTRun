package me.despical.tntrun.commands.game;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.despical.commonsbox.string.StringFormatUtils;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.user.User;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class StatsCommand extends SubCommand {

	public StatsCommand() {
		super("stats");
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
		Player player = args.length == 1 ? Bukkit.getPlayerExact(args[0]) : (Player) sender;

		if (player == null) {
			assert sender != null;
			sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Admin-Commands.Player-Not-Found"));
			return;
		}

		User user = getPlugin().getUserManager().getUser(player);

		if (player.equals(sender)) {
			sender.sendMessage(getPlugin().getChatManager().colorMessage("Commands.Stats-Command.Header", player));
		} else {
			sender.sendMessage(getPlugin().getChatManager().colorMessage("Commands.Stats-Command.Header-Other", player).replace("%player%", player.getName()));
		}

		sender.sendMessage(getPlugin().getChatManager().colorMessage("Commands.Stats-Command.Wins", player) + user.getStat(StatsStorage.StatisticType.WINS));
		sender.sendMessage(getPlugin().getChatManager().colorMessage("Commands.Stats-Command.Loses", player) + user.getStat(StatsStorage.StatisticType.LOSES));
		sender.sendMessage(getPlugin().getChatManager().colorMessage("Commands.Stats-Command.Games-Played", player) + user.getStat(StatsStorage.StatisticType.GAMES_PLAYED));
		sender.sendMessage(getPlugin().getChatManager().colorMessage("Commands.Stats-Command.Coins", player) + user.getStat(StatsStorage.StatisticType.COINS));
		sender.sendMessage(getPlugin().getChatManager().colorMessage("Commands.Stats-Command.Longest-Survive", player) + StringFormatUtils.formatIntoMMSS(user.getStat(StatsStorage.StatisticType.LONGEST_SURVIVE)));
		sender.sendMessage(getPlugin().getChatManager().colorMessage("Commands.Stats-Command.Footer", player));
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