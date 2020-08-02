package me.despical.tntrun.commands.admin.arena;

import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ForceStartCommand extends SubCommand {

	public ForceStartCommand() {
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
	public void execute(CommandSender sender, String label, String[] args) {
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
		return Collections.singletonList("Force start arena you're in");
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