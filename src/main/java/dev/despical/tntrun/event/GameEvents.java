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

package dev.despical.tntrun.event;

import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.user.User;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * @author Despical
 * <p>
 * Created at 4.02.2023
 */
public class GameEvents extends ListenerAdapter {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        User user = userManager.getUser(victim);
        Arena arena = user.getArena();

        if (arena == null) {
            return;
        }

        switch (event.getCause()) {
            case DROWNING, FALL -> event.setCancelled(true);
            case VOID -> {
                event.setCancelled(true);

                Game game = arena.getGame();
                if (!game.isState(GameState.IN_GAME)) {
                    victim.teleport(arena.getOption(ArenaKeys.LOBBY_LOCATION));
                    return;
                }

                if (user.isSpectator()) {
                    victim.teleport(game.getStartLocation());
                    return;
                }

                game.eliminate(user);
            }
        }
    }

    @EventHandler
    public void onLobbyDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        arenaRegistry.findArena(player).ifPresent(arena -> {
            if (arena.isState(GameState.IN_GAME)) {
                return;
            }

            event.setCancelled(true);
            player.setFireTicks(0);
        });
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = getAttackingPlayer(event.getDamager());
        if (attacker == null) return;

        Arena victimArena = arenaRegistry.getArena(victim);
        Arena attackerArena = arenaRegistry.getArena(attacker);
        if (victimArena == null && attackerArena == null) return;

        if (!canDamagePlayer(attacker, attackerArena, victim, victimArena)) {
            event.setCancelled(true);
            victim.setFireTicks(0);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) return;
        if (!(event.getDamager() instanceof Player player)) return;

        Arena arena = arenaRegistry.getArena(player);
        if (arena == null) return;

        event.setCancelled(true);
    }

    private boolean canDamagePlayer(Player attacker, Arena attackerArena, Player victim, Arena victimArena) {
        if (attackerArena == null || attackerArena != victimArena) {
            return false;
        }

        if (!attackerArena.getOption(ArenaKeys.ARENA_PVP_ENABLED)
            || !attackerArena.isState(GameState.IN_GAME)
            || !attackerArena.getGame().getBlockRemovalManager().hasRemovalStarted()) {
            return false;
        }

        User attackerUser = userManager.getUser(attacker);
        User victimUser = userManager.getUser(victim);

        return !attackerUser.isSpectator()
            && !victimUser.isSpectator()
            && !attackerArena.isDeathPlayer(attackerUser)
            && !victimArena.isDeathPlayer(victimUser);
    }

    private Player getAttackingPlayer(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }

        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }

        return null;
    }
}
