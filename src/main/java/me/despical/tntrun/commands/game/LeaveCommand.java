package me.despical.tntrun.commands.game;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.utils.Debugger;
import me.despical.tntrun.utils.Utils;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class LeaveCommand extends SubCommand {

	public LeaveCommand() {
		super("leave");
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
		if (!getPlugin().getConfig().getBoolean("Disable-Leave-Command", false)) {
			Player player = (Player) sender;
			if (!Utils.checkIsInGameInstance((Player) sender)) {
				return;
			}
			player.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Teleported-To-The-Lobby", player));
			if (getPlugin().getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
				getPlugin().getBungeeManager().connectToHub(player);
				Debugger.debug(Level.INFO, "{0} was teleported to the Hub server", player.getName());
				return;
			}
			Arena arena = ArenaRegistry.getArena(player);
			ArenaManager.leaveAttempt(player, arena);
			Debugger.debug(Level.INFO, "{0} has left the arena {1}! Teleported to end location.", player.getName(), arena.getId());
		}
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