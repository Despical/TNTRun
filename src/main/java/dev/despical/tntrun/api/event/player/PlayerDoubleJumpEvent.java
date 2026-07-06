/*
 * TNT Run - A fast-paced parkour minigame for Minecraft.
 * Copyright (C) 2026  Berke AkÃ§en
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
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a player consumes and performs a double jump.
 * <p>
 * The player has already passed the built-in game, spectator, cooldown, and
 * remaining-jump checks when this event is fired. Cancelling the event prevents
 * the jump from being consumed and stops the velocity, sound, cooldown, and
 * scoreboard updates from being applied.
 * <p>
 * Listeners may modify {@link #setVelocity(Vector)} and
 * {@link #setCooldownSeconds(double)} to customize the launch force or the next
 * double-jump cooldown.
 *
 * @author Despical
 * <p>
 * Created at 06.07.2026
 */
@Getter
public class PlayerDoubleJumpEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * Whether this double jump should be cancelled.
     */
    @Setter
    private boolean cancelled;

    /**
     * The game in which the double jump will be performed.
     */
    private final Game game;

    /**
     * The number of double jumps the player will have left after this jump is
     * consumed.
     */
    private final int jumpsLeft;

    /**
     * The cooldown, in seconds, to apply after the double jump.
     * <p>
     * Set this to {@code 0} or lower to leave the player without a cooldown.
     */
    private double cooldownSeconds;

    /**
     * The velocity that will be applied to the player.
     */
    private Vector velocity;

    /**
     * Constructs a new PlayerDoubleJumpEvent.
     *
     * @param player the player performing the double jump
     * @param game the game in which the double jump is being performed
     * @param jumpsLeft the number of double jumps left after this jump
     * @param cooldownSeconds the cooldown to apply after the jump
     * @param velocity the velocity to apply to the player
     */
    public PlayerDoubleJumpEvent(Player player, Game game, int jumpsLeft, double cooldownSeconds, @NotNull Vector velocity) {
        super(player);
        this.game = game;
        this.jumpsLeft = jumpsLeft;
        this.cooldownSeconds = cooldownSeconds;
        this.velocity = velocity.clone();
    }

    /**
     * Replaces the cooldown that will be applied after the double jump.
     *
     * @param cooldownSeconds the new cooldown in seconds
     */
    public void setCooldownSeconds(double cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    /**
     * Replaces the velocity that will be applied to the player.
     *
     * @param velocity the new velocity
     */
    public void setVelocity(@NotNull Vector velocity) {
        this.velocity = velocity.clone();
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
