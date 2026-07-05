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

package dev.despical.tntrun.api.event;

import org.bukkit.event.Event;

/**
 * Base type for all Bukkit events fired by TNTRun.
 * <p>
 * Each concrete event class owns its own Bukkit {@code HandlerList}. This base
 * class is only a shared marker and should not be listened to directly.
 *
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public abstract class TNTRunEvent extends Event {
}
