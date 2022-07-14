package me.despical.tntrun.commands.player;

import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public class StatsCommand extends SubCommand {

	public StatsCommand() {
		super ("stats");
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
		Player player = args.length == 0 ? (Player) sender : plugin.getServer().getPlayer(args[0]);
		String path = "commands.stats_command.";

		if (player == null) {
			sender.sendMessage(chatManager.prefixedMessage(path + "player_not_found"));
			return;
		}

		User user = plugin.getUserManager().getUser(player);

		if (player.equals(sender)) {
			player.sendMessage(chatManager.message(path + "header", player));
		} else {
			player.sendMessage(chatManager.message(path + "header_other", player).replace("%player%", player.getName()));
		}

		sender.sendMessage(chatManager.message(path + "wins", player) + user.getStat(StatsStorage.StatisticType.WINS));
		sender.sendMessage(chatManager.message(path + "loses", player) + user.getStat(StatsStorage.StatisticType.LOSES));
		sender.sendMessage(chatManager.message(path + "coins", player) + user.getStat(StatsStorage.StatisticType.COINS));
		sender.sendMessage(chatManager.message(path + "games_played", player) + user.getStat(StatsStorage.StatisticType.GAMES_PLAYED));
		sender.sendMessage(chatManager.message(path + "longest_survive", player) + user.getStat(StatsStorage.StatisticType.LONGEST_SURVIVE));
		sender.sendMessage(chatManager.message(path + "footer", player));
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
	public SenderType getSenderType() {
		return SubCommand.SenderType.PLAYER;
	}
}