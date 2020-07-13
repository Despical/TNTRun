package me.despical.tntrun.commands.admin.arena;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.commands.exception.CommandException;
import me.despical.tntrun.utils.Utils;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ForceStartCommand extends SubCommand {

	public ForceStartCommand(String name) {
		super("forcestart");
		setPermission("tntrun.admin.forcestart");
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
		if (!Utils.checkIsInGameInstance((Player) sender)) {
			return;
		}
		Arena arena = ArenaRegistry.getArena((Player) sender);
		if (arena.getPlayers().size() < 2) {
			getPlugin().getChatManager().broadcast(arena, getPlugin().getChatManager().formatMessage(arena, getPlugin().getChatManager().colorMessage("In-Game.Messages.Lobby-Messages.Waiting-For-Players"), arena.getMinimumPlayers()));
			return;
		}
		if (arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS || arena.getArenaState() == ArenaState.STARTING) {
			arena.setArenaState(ArenaState.STARTING);
			arena.setForceStart(true);
			arena.setTimer(0);
			for (Player p : ArenaRegistry.getArena((Player) sender).getPlayers()) {
				p.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("In-Game.Messages.Admin-Messages.Set-Starting-In-To-0"));
			}
		}
	}

	@Override
	public List<String> getTutorial() {
		return Arrays.asList("Force start arena you're in");
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