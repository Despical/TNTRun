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

package me.despical.tntrun.api.events.player;

import me.despical.tntrun.api.events.TNTRunEvent;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.user.User;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called when a player gets eliminated in a game.
 *
 * @author Despical
 * <p>
 * Created at 5.10.2024
 */
public class PlayerEliminatedEvent extends TNTRunEvent {

    private static final HandlerList handlerList = new HandlerList();

    private final User user;

    public PlayerEliminatedEvent(Arena arena, User user) {
        super(arena);
        this.user = user;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public User getEliminatedPlayer() {
        return user;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}