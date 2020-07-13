package me.despical.tntrun.commands.admin.arena;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import me.despical.commonsbox.serializer.InventorySerializer;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.commands.exception.CommandException;
import me.despical.tntrun.utils.Debugger;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ReloadCommand extends SubCommand {

	public ReloadCommand(String name) {
		super("reload");
		setPermission("tntrun.admin.reload");
	}

	private Set<CommandSender> confirmations = new HashSet<>();

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
		if (!confirmations.contains(sender)) {
			confirmations.add(sender);
			Bukkit.getScheduler().runTaskLater(getPlugin(), () -> confirmations.remove(sender), 20 * 10);
			sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Are-You-Sure"));
			return;
		}
		confirmations.remove(sender);
		Debugger.debug(Level.INFO, "Initiated plugin reload by {0}", sender.getName());
		long start = System.currentTimeMillis();

		getPlugin().reloadConfig();
		getPlugin().getChatManager().reloadConfig();

		for (Arena arena : ArenaRegistry.getArenas()) {
			Debugger.debug(Level.INFO, "[Reloader] Stopping {0} instance.");
			long stopTime = System.currentTimeMillis();
			for (Player player : arena.getPlayers()) {
				arena.doBarAction(Arena.BarAction.REMOVE, player);
				if (getPlugin().getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(getPlugin(), player);
				} else {
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					for (PotionEffect pe : player.getActivePotionEffects()) {
						player.removePotionEffect(pe.getType());
					}
					player.setWalkSpeed(0.2f);
				}
			}
			ArenaManager.stopGame(true, arena);
			Debugger.debug(Level.INFO, "[Reloader] Instance {0} stopped took {1} ms", arena.getId(), System.currentTimeMillis() - stopTime);
		}
		ArenaRegistry.registerArenas();
		sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Admin-Commands.Success-Reload"));
		Debugger.debug(Level.INFO, "[Reloader] Finished reloading took {0} ms", System.currentTimeMillis() - start);
	}

	@Override
	public List<String> getTutorial() {
		return Arrays.asList("Reload all game arenas and configurations", "All of the arenas will be stopped!");
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