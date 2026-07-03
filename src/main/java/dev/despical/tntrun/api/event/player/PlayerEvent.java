package dev.despical.tntrun.api.event.player;

import dev.despical.tntrun.Main;
import dev.despical.tntrun.api.event.TNTRunEvent;
import dev.despical.tntrun.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event related to a specific player in TNTRun.
 * <p>
 * Provides convenient access to the {@link Player} object and the corresponding
 * {@link User} instance used internally by the plugin.
 * <p>
 * Example subclasses and their purposes:
 * <ul>
 *     <li>{@link PlayerJoinAttemptEvent} – fired when a player attempts to join a game (cancellable)</li>
 *     <li>{@link PlayerLeaveGameEvent} – fired when a player leaves a game</li>
 *     <li>{@link PlayerStatisticChangeEvent} – fired when a player's statistic changes</li>
 * </ul>
 * <p>
 * Use {@link #getPlayer()} for Bukkit-level operations, and
 * {@link #getUser()} for plugin-specific data.
 *
 * @author Despical
 * @since 29.01.2026
 */
@Getter
@AllArgsConstructor
public abstract class PlayerEvent extends TNTRunEvent {

    /** The Bukkit player associated with this event */
    @NotNull
    private final Player player;

    /**
     * Returns the plugin-specific {@link User} for this player.
     *
     * @return the User object representing the player
     */
    @NotNull
    public final User getUser() {
        return Main.getInstance().getUserManager().getUser(player);
    }

    @Override
    public String toString() {
        return "[player=%s]".formatted(player.getName());
    }
}
