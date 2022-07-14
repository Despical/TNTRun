package me.despical.tntrun.commands.admin;

import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.handlers.setup.SetupInventory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public class EditCommand extends SubCommand {

	public EditCommand() {
		super ("edit");

		setPermission("tntrun.admin.edit");
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
	public void execute(CommandSender sender, String label, String[] args) {
		Arena arena = ArenaRegistry.getArena(args[0]);

		if (arena == null) {
			sender.sendMessage(chatManager.prefixedMessage("Commands.no_arena_like_that"));
			return;
		}

		new SetupInventory(plugin, arena, (Player) sender).openInventory();
	}

	@Override
	public String getTutorial() {
		return "Opens the arena editor";
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