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

package me.despical.tntrun.leaderboard;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 1.11.2024
 */
public class Leaderboard {

	private Map<UUID, Integer> entries;

	public Leaderboard() {
		this.entries = new LinkedHashMap<>();
	}

	public void addEntry(UUID uniqueId, int value) {
		entries.put(uniqueId, value);
	}

	public Map.Entry<UUID, Integer> getEntry(int placement) {
		return entries.entrySet().stream()
			.skip(placement - 1)
			.findFirst()
			.orElse(null);
	}

	void sort() {
		this.entries = entries.entrySet()
			.stream()
			.sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue,
				(e1, e2) -> e1,
				LinkedHashMap::new
			));
	}
}