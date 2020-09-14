package me.despical.tntrun.commands.admin.arena;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.utils.Utils;

import static me.despical.tntrun.arena.ArenaRegistry.getArena;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class StopCommand extends SubCommand {

	public StopCommand() {
		super("stop");
		setPermission("tntrun.admin.stop");
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

		if (getArena((Player) sender).getArenaState() != ArenaState.ENDING) {
			ArenaManager.stopGame(true, getArena((Player) sender));
		}
	}

	@Override
	public List<String> getTutorial() {
		return Arrays.asList("Stop the arena you're in", "You must be in target arena!");
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