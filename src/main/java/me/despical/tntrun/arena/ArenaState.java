/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2023 Despical
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

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public enum ArenaState {

	WAITING_FOR_PLAYERS("Waiting"),
	STARTING("Starting"),
	IN_GAME("Playing"),
	ENDING("Ending"),
	RESTARTING("Restarting"),
	INACTIVE("Inactive");

	final String formattedName;

	ArenaState(String formattedName) {
		this.formattedName = formattedName;
	}

	public String getFormattedName() {
		return formattedName;
	}
}