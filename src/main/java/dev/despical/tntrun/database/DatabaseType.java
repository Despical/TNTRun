/*
TNT Run - Fast-paced arena survival for Minecraft.
Copyright (C) 2026  Berke Akçen

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package dev.despical.tntrun.database;

import java.util.Arrays;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public enum DatabaseType {

    FLAT_FILE("Flat File", "flat", "flatfile", "default"),
    MYSQL("MySQL", "mysql", "sql");

    private final List<String> names;

    DatabaseType(String... names) {
        this.names = Arrays.asList(names);
    }

    public String getName() {
        return names.getFirst();
    }

    public static DatabaseType getByName(String name) {
        return Arrays.stream(values())
            .filter(type -> type.names.contains(name))
            .findFirst()
            .orElse(null);
    }
}
