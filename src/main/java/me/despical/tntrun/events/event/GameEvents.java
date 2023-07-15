package me.despical.tntrun.events.event;

import me.despical.commons.compat.XMaterial;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.events.EventListener;
import me.despical.tntrun.user.User;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

/**
 * @author Despical
 * <p>
 * Created at 4.02.2023
 */
public class GameEvents extends EventListener {

	public GameEvents(Main plugin) {
		super(plugin);
	}

	@EventHandler
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;

		final var user = plugin.getUserManager().getUser(player);

		if (user.isInArena()) event.setCancelled(true);
	}

	@EventHandler
	public void onDropItemEvent(PlayerDropItemEvent event) {
		final var user = plugin.getUserManager().getUser(event.getPlayer());

		if (user.isInArena()) event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player victim)) return;

		final var user = plugin.getUserManager().getUser(victim);
		final var arena = user.getArena();

		if (arena == null) return;

		switch (e.getCause()) {
			case DROWNING, FALL -> e.setCancelled(true);
			case VOID -> {
				victim.teleport(arena.getLobbyLocation());

				if (!user.isSpectator()) {
					user.setSpectator(true);
					user.playDeathEffect();

					arena.addDeathPlayer(user);

					user.addGameItems("leave-item", "settings-item", "teleporter-item");

					final var playersLeft = arena.getPlayersLeft();

					if (playersLeft.size() == 1) {
						arena.getWinners().add(arena.getWinner());

						plugin.getArenaManager().stopGame(false, arena);
						return;
					}

					arena.broadcastFormattedMessage("messages.in-game.fell-into-void", user);
				}
			}
		}
	}

	@EventHandler
	public void onItemMove(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player player && plugin.getUserManager().getUser(player).isInArena()) {
			if (e.getView().getType() == InventoryType.CRAFTING || e.getView().getType() == InventoryType.PLAYER) {
				e.setResult(Event.Result.DENY);
			}
		}
	}

	@EventHandler
	public void onCraft(PlayerInteractEvent event) {
		final var player = event.getPlayer();
		final var user = plugin.getUserManager().getUser(player);

		if (!user.isInArena()) return;

		if (player.getTargetBlock(null, 7).getType() == XMaterial.CRAFTING_TABLE.parseMaterial()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onItemSwap(PlayerSwapHandItemsEvent event) {
		final var user = plugin.getUserManager().getUser(event.getPlayer());

		if (!user.isInArena()) return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		final var user = plugin.getUserManager().getUser(event.getPlayer());

		if (!user.isInArena()) return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		final var user = plugin.getUserManager().getUser(event.getPlayer());

		if (user.isInArena()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onLobbyDamage(EntityDamageEvent event) {
		if (event.getEntity().getType() != EntityType.PLAYER) return;

		final var player = (Player) event.getEntity();
		final var user = plugin.getUserManager().getUser(player);
		final var arena = user.getArena();

		if (arena == null) return;

		event.setCancelled(true);
		player.setFireTicks(0);
	}

	@EventHandler
	public void onChatEvent(AsyncPlayerChatEvent event) {
		final var user = plugin.getUserManager().getUser(event.getPlayer());
		final var arena = user.getArena();

		if (arena == null) {
			if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DISABLE_SEPARATE_CHAT)) {
				plugin.getArenaRegistry().getArenas().forEach(loopArena -> loopArena.getPlayers().forEach(u -> event.getRecipients().remove(u.getPlayer())));
			}

			return;
		}

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.CHAT_FORMAT_ENABLED)) {
			var message = formatChatPlaceholders(chatManager.message("messages.in-game.game-chat-format"), user, event.getMessage());

			if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DISABLE_SEPARATE_CHAT)) {
				event.setCancelled(true);

				var dead = arena.isDeathPlayer(user) || arena.isSpectator(user);

				for (final var u : arena.getPlayers()) {
					if (dead && arena.getPlayersLeft().contains(u)) continue;

					if (dead) {
						String prefix = formatChatPlaceholders(chatManager.message("messages.in-game.game-death-format"), user, event.getMessage());
						u.sendRawMessage(prefix + message);
					} else {
						u.sendRawMessage(message);
					}
				}

				plugin.getServer().getConsoleSender().sendMessage(message);
			} else {
				event.setMessage(message);
			}
		}
	}

	private String formatChatPlaceholders(final String message, final User user, final String saidMessage) {
		var formatted = message;

		formatted = formatted.replace("%player%", user.getPlayer().getName());
		formatted = formatted.replace("%message%", ChatColor.stripColor(saidMessage));

		return chatManager.rawMessage(formatted);
	}
}