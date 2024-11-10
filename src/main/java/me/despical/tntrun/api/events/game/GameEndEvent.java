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

package me.despical.tntrun.api.events.game;

import me.despical.tntrun.api.events.TNTRunEvent;
import me.despical.tntrun.arena.Arena;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called when the game ends.
 *
 * @author Despical
 * <p>
 * Created at 23.07.2023
 */
public class GameEndEvent extends TNTRunEvent {

    private static final HandlerList handlerList = new HandlerList();

    private final boolean quickStop;

    public GameEndEvent(Arena arena, boolean quickStop) {
        super(arena);
        this.quickStop = quickStop;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public boolean isQuickStop() {
        return this.quickStop;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
