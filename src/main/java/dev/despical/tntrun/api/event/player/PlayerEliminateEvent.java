/*
 * TNT Run - A fast-paced parkour minigame for Minecraft.
 * Copyright (C) 2026  Berke Akçen
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

package dev.despical.tntrun.api.event.player;

import dev.despical.tntrun.game.Game;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a player is eliminated from an active TNTRun game.
 * <p>
 * The player has already been marked as a spectator when this event is fired.
 * Use this event for rewards, custom announcements, analytics, or external
 * integrations that need to react to eliminations.
 *
 * @author Despical
 * <p>
 * Created at 06.07.2026
 */
@Getter
public class PlayerEliminateEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The game the player was eliminated from.
     */
    private final Game game;

    /**
     * The number of non-spectator players left after this elimination.
     */
    private final int playersLeft;

    /**
     * Constructs a new PlayerEliminateEvent.
     *
     * @param player the eliminated player
     * @param game the game the player was eliminated from
     * @param playersLeft the number of players left after the elimination
     */
    public PlayerEliminateEvent(Player player, Game game, int playersLeft) {
        super(player);
        this.game = game;
        this.playersLeft = playersLeft;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
