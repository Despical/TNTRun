/*
 * TNT Run - A Minecraft parkour minigame
 * Copyright (C) 2024  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.tntrun.arena;

import me.despical.tntrun.api.events.game.GameStateChangeEvent;
import me.despical.tntrun.arena.options.ArenaOption;
import me.despical.tntrun.user.User;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class Arena extends ArenaHandler {

	public Arena(final @NotNull String id) {
		super(id);
	}

	@Override
	public void setArenaState(ArenaState arenaState) {
		final var event = new GameStateChangeEvent(this, this.arenaState);

		this.arenaState = arenaState;
		this.gameBarManager.handleGameBar();
		this.updateSigns();

		plugin.getServer().getPluginManager().callEvent(event);
	}

	@Override
	public void broadcastFormattedMessage(final String path, final User user, boolean onlySpectators) {
		if (!onlySpectators) {
			this.broadcastFormattedMessage(path, user);
			return;
		}

		if (user.isSpectator()) {
			this.getPlayers().stream().filter(u -> isSpectator(u) && !user.equals(u)).forEach(u -> u.sendRawMessage(chatManager.message(path, this, user)));
		}
	}

	@Override
	public void broadcastFormattedMessage(final String path, final User user) {
		this.getPlayers().forEach(u -> u.sendRawMessage(chatManager.message(path, this, user)));
	}

	@Override
	public void broadcastMessage(final String path, Object... params) {
		this.getPlayers().forEach(user -> user.sendRawMessage(MessageFormat.format(chatManager.message(path, this, user), params)));
	}

	@Override
	public void updateSigns() {
		Optional.ofNullable(plugin.getSignManager()).ifPresent(signManager -> signManager.updateSign(this));
	}

	public void start() {
		this.startBlockRemoving();
		this.runTaskTimer(plugin, 20L, 20L);
		this.setArenaState(ArenaState.WAITING_FOR_PLAYERS);
	}

	public void stop() {
		this.setOption(ArenaOption.STOPPED, true);

		if (arenaState != ArenaState.INACTIVE) this.cancel();

		this.cleanUpArena();
		this.getPlayers().forEach(user -> plugin.getArenaManager().leaveAttempt(user, this));
	}
}