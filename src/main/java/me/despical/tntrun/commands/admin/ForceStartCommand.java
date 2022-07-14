package me.despical.tntrun.commands.admin;

import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public class ForceStartCommand extends SubCommand {

	public ForceStartCommand() {
		super ("forcestart");

		setPermission("tntrun.admin.forcestart");
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
		Player player = (Player) sender;
		Arena arena = ArenaRegistry.getArena(player);

		if (arena == null) {
			player.sendMessage(chatManager.prefixedMessage("commands.not_playing", player));
			return;
		}

		if (arena.getPlayers().size() < 2) {
			arena.broadcastMessage(chatManager.prefixedFormattedMessage(arena, "in_game.messages.lobby_messages.waiting_for_players", arena.getMinimumPlayers()));
			return;
		}

		if (arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS || arena.getArenaState() == ArenaState.STARTING) {
			arena.setTimer(0);
			arena.setForceStart(true);
			arena.setArenaState(ArenaState.STARTING);
			arena.broadcastMessage(chatManager.prefixedMessage("in_game.messages.admin_messages.set_starting_in_to_0"));
		}
	}

	@Override
	public String getTutorial() {
		return "Force start arena that user in";
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