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

package dev.despical.tntrun.command.arguments;

import dev.despical.commandframework.CommandArguments;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Var;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 03.07.2026
 */
public final class Arguments extends CommandArguments {

    private static final Main plugin = Main.getInstance();

    private final CommandArguments arguments;

    public Arguments(CommandArguments arguments) {
        super(arguments);
        this.arguments = arguments;
    }

    public User getUser() {
        return plugin.getUserManager().getUser(arguments.<Player>getSender());
    }

    public void sendMessage(String messageKey, Var... vars) {
        plugin.getChatManager().sendMessage(this, messageKey, vars);
    }

    @Override
    public void sendMessage(String messageKey) {
        plugin.getChatManager().sendMessage(this, messageKey);
    }

    public void sendBlankMessage() {
        arguments.sendMessage("");
    }

    public void sendRawMessage(String message, Var... vars) {
        plugin.getChatManager().sendRawMessage(arguments.getSender(), message, vars);
    }

    public void sendCenteredMessage(String path, Var... vars) {
        plugin.getChatManager().sendCenteredMessage(arguments.getSender(), path, vars);
    }

    public void playSound(Sound sound, float volume, float pitch) {
        Player player = arguments.getSender();
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
