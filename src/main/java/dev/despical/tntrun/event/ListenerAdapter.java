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

package dev.despical.tntrun.event;

import dev.despical.fileitems.ItemManager;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.arena.ArenaRegistry;
import dev.despical.tntrun.arena.managers.ArenaManager;
import dev.despical.tntrun.chat.ChatManager;
import dev.despical.tntrun.game.GameManager;
import dev.despical.tntrun.option.ConfigOptions;
import dev.despical.tntrun.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public abstract class ListenerAdapter implements Listener {

    protected static final Main plugin = Main.getInstance();

    protected final ConfigOptions options;
    protected final ArenaManager arenaManager;
    protected final ArenaRegistry arenaRegistry;
    protected final UserManager userManager;
    protected final ItemManager itemManager;
    protected final ChatManager chatManager;
    protected final GameManager gameManager;

    public ListenerAdapter() {
        this.options = plugin.getOptions();
        this.arenaManager = plugin.getArenaManager();
        this.arenaRegistry = plugin.getArenaRegistry();
        this.userManager = plugin.getUserManager();
        this.itemManager = plugin.getItemManager();
        this.chatManager = plugin.getChatManager();
        this.gameManager = plugin.getGameManager();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
